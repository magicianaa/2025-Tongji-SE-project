package com.esports.hotel.interceptor;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.common.ResultCode;
import com.esports.hotel.config.JwtProperties;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.mapper.CheckInRecordMapper;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * 二次鉴权拦截器
 * 
 * 核心逻辑：
 * 1. 验证用户已登录（JWT Token有效）
 * 2. 通过 user_id 查询对应的 guest_id
 * 3. 验证用户有有效的入住记录（actual_checkin <= NOW <= expected_checkout）
 * 4. 验证客房权限未被回收（is_gaming_auth_active = true）
 * 5. 将房间信息注入到 Request Attribute 中，供后续业务使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final CheckInRecordMapper checkInRecordMapper;
    private final GuestMapper guestMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 检查是否标注了 @RoomAuthRequired 注解
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RoomAuthRequired annotation = handlerMethod.getMethodAnnotation(RoomAuthRequired.class);
        if (annotation == null) {
            // 方法未标注，检查类级别
            annotation = handlerMethod.getBeanType().getAnnotation(RoomAuthRequired.class);
            if (annotation == null) {
                return true; // 无需鉴权
            }
        }

        // 2. 提取并验证 Token
        String authHeader = request.getHeader(jwtProperties.getHeaderName());
        String token = jwtUtil.extractToken(authHeader);
        
        if (token == null || !jwtUtil.validateToken(token)) {
            log.warn("二次鉴权失败: Token无效或已过期");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 3. 从 Token 中提取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 4. 通过 user_id 查询对应的 guest_id
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getUserId, userId);
        Guest guest = guestMapper.selectOne(guestWrapper);
        
        if (guest == null) {
            log.warn("二次鉴权失败: 用户 {} 不是住客类型用户", userId);
            throw new BusinessException(ResultCode.FORBIDDEN, "该功能仅对住客开放");
        }
        
        Long guestId = guest.getGuestId();

        // 5. 查询住客当前的有效入住记录
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getGuestId, guestId)
               .le(CheckInRecord::getActualCheckin, now)
               .ge(CheckInRecord::getExpectedCheckout, now)
               .eq(CheckInRecord::getIsGamingAuthActive, true)
               .isNull(CheckInRecord::getActualCheckout)
               .last("LIMIT 1");

        CheckInRecord record = checkInRecordMapper.selectOne(wrapper);

        if (record == null) {
            log.warn("二次鉴权失败: 住客 {} (userId={}) 无有效入住记录", guestId, userId);
            throw new BusinessException(ResultCode.NO_CHECKIN_RECORD, "请先入住并绑定房间");
        }

        // 6. 验证通过，将关键信息注入到 Request Attribute
        request.setAttribute("userId", userId);
        request.setAttribute("guestId", guestId);
        request.setAttribute("recordId", record.getRecordId());
        request.setAttribute("roomId", record.getRoomId());

        log.info("二次鉴权通过: userId={}, guestId={}, roomId={}, recordId={}", 
                userId, guestId, record.getRoomId(), record.getRecordId());
        return true;
    }
}

package com.esports.hotel.interceptor;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.common.ResultCode;
import com.esports.hotel.config.JwtProperties;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.mapper.CheckInRecordMapper;
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
 * 2. 验证用户有有效的入住记录（actual_checkin <= NOW <= expected_checkout）
 * 3. 验证客房权限未被回收（is_gaming_auth_active = true）
 * 4. 将房间信息注入到 Request Attribute 中，供后续业务使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final CheckInRecordMapper checkInRecordMapper;

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

        // 4. 查询用户当前的有效入住记录
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getGuestId, userId)
               .le(CheckInRecord::getActualCheckin, now)
               .ge(CheckInRecord::getExpectedCheckout, now)
               .eq(CheckInRecord::getIsGamingAuthActive, true)
               .isNull(CheckInRecord::getActualCheckout)
               .last("LIMIT 1");

        CheckInRecord record = checkInRecordMapper.selectOne(wrapper);

        if (record == null) {
            log.warn("二次鉴权失败: 用户 {} 无有效入住记录", userId);
            throw new BusinessException(ResultCode.NO_CHECKIN_RECORD);
        }

        // 5. 验证通过，将关键信息注入到 Request Attribute
        request.setAttribute("userId", userId);
        request.setAttribute("recordId", record.getRecordId());
        request.setAttribute("roomId", record.getRoomId());
        request.setAttribute("guestId", record.getGuestId());

        log.debug("二次鉴权通过: userId={}, roomId={}, recordId={}", userId, record.getRoomId(), record.getRecordId());
        return true;
    }
}

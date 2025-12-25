package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.common.ResultCode;
import com.esports.hotel.dto.LoginRequest;
import com.esports.hotel.dto.LoginResponse;
import com.esports.hotel.dto.RegisterRequest;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.User;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.UserMapper;
import com.esports.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final GuestMapper guestMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final com.esports.hotel.mapper.CheckInRecordMapper checkInRecordMapper;
    private final com.esports.hotel.mapper.RoomMapper roomMapper;

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 1. 验证短信验证码（模拟验证，生产环境需对接真实短信平台）
        String cachedCode = redisTemplate.opsForValue().get("sms:code:" + request.getPhone());
        if (cachedCode == null || !cachedCode.equals(request.getSmsCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, request.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        // 3. 创建用户账号
        User user = new User();
        user.setUsername(request.getPhone());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType("GUEST");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 4. 创建住客扩展信息（姓名和身份证在首次入住时绑定）
        Guest guest = new Guest();
        guest.setUserId(user.getUserId());
        guest.setRealName(null); // 入住时绑定
        guest.setMemberLevel("BRONZE"); // 默认青铜会员
        guest.setExperiencePoints(0);
        guest.setCurrentPoints(0);
        guest.setTotalCheckinNights(0);
        guestMapper.insert(guest);

        // 5. 删除已使用的验证码
        redisTemplate.delete("sms:code:" + request.getPhone());

        log.info("用户注册成功: userId={}, phone={}", user.getUserId(), request.getPhone());
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 检查账号状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 生成普通 Token（仅基础浏览权限）
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserType());

        // 5. 构造响应
        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setUserId(user.getUserId());
        response.setUserType(user.getUserType());
        response.setUsername(user.getUsername());

        // 6. 如果是住客，附加会员信息
        if ("GUEST".equals(user.getUserType())) {
            LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
            guestWrapper.eq(Guest::getUserId, user.getUserId());
            Guest guest = guestMapper.selectOne(guestWrapper);
            if (guest != null) {
                response.setMemberLevel(guest.getMemberLevel());
                response.setCurrentPoints(guest.getCurrentPoints());
            }
        }

        log.info("用户登录成功: userId={}, userType={}", user.getUserId(), user.getUserType());
        return response;
    }

    /**
     * 检查入住状态
     */
    public com.esports.hotel.common.Result<?> checkInStatus(String authHeader) {
        log.info("========== 开始检查入住状态 ==========");
        log.info("Authorization Header: {}", authHeader);
        
        // 1. 提取并验证 Token
        String token = jwtUtil.extractToken(authHeader);
        if (token == null) {
            log.warn("检查入住状态：未提供Token");
            return com.esports.hotel.common.Result.success(java.util.Map.of(
                "hasCheckIn", false,
                "message", "未提供认证Token"
            ));
        }
        
        log.info("提取到Token: {}...", token.substring(0, Math.min(20, token.length())));
        
        if (!jwtUtil.validateToken(token)) {
            log.warn("检查入住状态：Token验证失败");
            return com.esports.hotel.common.Result.success(java.util.Map.of(
                "hasCheckIn", false,
                "message", "Token无效或已过期"
            ));
        }
        
        log.info("Token验证通过");

        // 2. 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("检查入住状态：无法从Token中获取用户ID");
            return com.esports.hotel.common.Result.success(java.util.Map.of(
                "hasCheckIn", false,
                "message", "Token格式错误"
            ));
        }

        // 3. 查询用户类型
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 4. 如果不是住客，直接返回无权限
        if (!"GUEST".equals(user.getUserType())) {
            return com.esports.hotel.common.Result.success(java.util.Map.of(
                "hasCheckIn", false,
                "message", "非住客用户无需客房权限"
            ));
        }

        // 5. 查询对应的 guest_id
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getUserId, userId);
        Guest guest = guestMapper.selectOne(guestWrapper);
        
        if (guest == null) {
            return com.esports.hotel.common.Result.success(java.util.Map.of(
                "hasCheckIn", false,
                "message", "未找到住客信息"
            ));
        }

        // 6. 查询有效的入住记录
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<com.esports.hotel.entity.CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.esports.hotel.entity.CheckInRecord::getGuestId, guest.getGuestId())
               .le(com.esports.hotel.entity.CheckInRecord::getActualCheckin, now)
               .ge(com.esports.hotel.entity.CheckInRecord::getExpectedCheckout, now)
               .eq(com.esports.hotel.entity.CheckInRecord::getIsGamingAuthActive, true)
               .isNull(com.esports.hotel.entity.CheckInRecord::getActualCheckout)
               .last("LIMIT 1");

        com.esports.hotel.entity.CheckInRecord record = checkInRecordMapper.selectOne(wrapper);

        if (record == null) {
            log.info("用户 {} (guestId={}) 无有效入住记录", userId, guest.getGuestId());
            return com.esports.hotel.common.Result.success(java.util.Map.of(
                "hasCheckIn", false,
                "message", "当前无有效入住记录"
            ));
        }

        // 7. 查询房间信息
        com.esports.hotel.entity.Room room = roomMapper.selectById(record.getRoomId());
        String roomNo = room != null ? room.getRoomNo() : String.valueOf(record.getRoomId());
        
        // 8. 有有效入住记录，返回房间信息
        log.info("用户 {} (guestId={}) 有效入住记录: roomId={}, roomNo={}, recordId={}", 
                userId, guest.getGuestId(), record.getRoomId(), roomNo, record.getRecordId());
        
        return com.esports.hotel.common.Result.success(java.util.Map.of(
            "hasCheckIn", true,
            "recordId", record.getRecordId(),
            "roomId", record.getRoomId(),
            "roomNo", roomNo,
            "guestId", guest.getGuestId(),
            "expectedCheckout", record.getExpectedCheckout().toString(),
            "message", "入住状态有效"
        ));
    }

    /**
     * 发送短信验证码（模拟实现）
     */
    public String sendSmsCode(String phone) {
        // 生成6位随机验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        
        // 存储到 Redis，5分钟有效
        redisTemplate.opsForValue().set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);
        
        // 生产环境需调用真实短信平台API
        log.info("【模拟短信】手机号: {}, 验证码: {} (5分钟内有效)", phone, code);
        
        // 返回验证码（仅模拟项目使用）
        return code;
    }
}

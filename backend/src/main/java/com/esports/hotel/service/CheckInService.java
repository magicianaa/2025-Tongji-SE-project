package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.CheckInRequest;
import com.esports.hotel.dto.CheckInResponse;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import com.esports.hotel.util.AesUtil;
import com.esports.hotel.util.JwtUtil;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 入住登记服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final RoomMapper roomMapper;
    private final UserMapper userMapper;
    private final GuestMapper guestMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final BookingMapper bookingMapper;
    private final AesUtil aesUtil;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 默认密码（用于自动注册）
    private static final String DEFAULT_PASSWORD = "Test123456";
    // 使用已验证的BCrypt哈希（对应Test123456）
    private static final String DEFAULT_PASSWORD_HASH = "$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu";

    /**
     * 办理多人入住登记
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckInResponse checkIn(CheckInRequest request) {
        // 1. 查询房间
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getRoomNo, request.getRoomNo());
        Room room = roomMapper.selectOne(roomWrapper);

        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        // 2. 验证房间状态
        if (!"VACANT".equals(room.getStatus())) {
            throw new BusinessException("房间当前状态不可入住: " + room.getStatus());
        }

        // 3. 验证入住人数
        int guestCount = request.getGuests().size();
        if (guestCount > room.getMaxOccupancy()) {
            throw new BusinessException(String.format(
                    "入住人数(%d人)超过房间最大容纳人数(%d人)", 
                    guestCount, room.getMaxOccupancy()));
        }

        // 4. 验证预订信息（如果房间有预订）
        LocalDateTime now = LocalDateTime.now();
        log.info("开始检查预订信息: roomId={}, roomNo={}, 当前时间={}", 
                room.getRoomId(), room.getRoomNo(), now);
        
        Booking activeBooking = bookingMapper.selectActiveBookingWithGuestName(
                room.getRoomId(), LocalDate.now());

        if (activeBooking != null) {
            // 房间有预订，需要验证住客信息和时间段
            log.info("找到今日预订记录: bookingId={}, guestId={}, mainGuestName={}, plannedCheckin={}, plannedCheckout={}", 
                    activeBooking.getBookingId(), activeBooking.getGuestId(), activeBooking.getMainGuestName(),
                    activeBooking.getPlannedCheckin(), activeBooking.getPlannedCheckout());
            
            // 4.1 验证时间段：当前时间必须在预订的时间范围内（允许提前1小时或延后1小时办理）
            LocalDateTime bookingStart = activeBooking.getPlannedCheckin().minusHours(1);
            LocalDateTime bookingEnd = activeBooking.getPlannedCheckout().plusHours(1);
            
            if (now.isBefore(bookingStart) || now.isAfter(bookingEnd)) {
                throw new BusinessException(
                    String.format("与预订信息不符，无法办理入住。预订入住时间：%s，预订退房时间：%s，当前时间：%s",
                        activeBooking.getPlannedCheckin().toLocalDate(),
                        activeBooking.getPlannedCheckout().toLocalDate(),
                        now.toLocalDate()));
            }
            
            // 4.2 验证住客信息：手机号和姓名必须匹配
            boolean matchFound = false;
            String matchedPhone = null;
            String matchedName = null;
            
            for (CheckInRequest.GuestInfo guestInfo : request.getGuests()) {
                // 查询该手机号对应的用户
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(User::getPhone, guestInfo.getPhone());
                User user = userMapper.selectOne(userWrapper);
                
                if (user != null) {
                    // 查询该用户对应的guest记录
                    LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
                    guestWrapper.eq(Guest::getUserId, user.getUserId());
                    Guest guest = guestMapper.selectOne(guestWrapper);
                    
                    log.info("验证住客: phone={}, realName={}, guestId={}, 预订guestId={}, 预订mainGuestName={}", 
                            guestInfo.getPhone(), guestInfo.getRealName(), 
                            guest != null ? guest.getGuestId() : null, 
                            activeBooking.getGuestId(),
                            activeBooking.getMainGuestName());
                    
                    // 验证：guest_id是否是预订者 且 姓名匹配
                    if (guest != null &&
                        guest.getGuestId().equals(activeBooking.getGuestId()) &&
                        guestInfo.getRealName().equals(activeBooking.getMainGuestName())) {
                        matchFound = true;
                        matchedPhone = guestInfo.getPhone();
                        matchedName = guestInfo.getRealName();
                        log.info("预订验证通过！匹配的住客: phone={}, name={}", matchedPhone, matchedName);
                        break;
                    }
                }
            }
            
            if (!matchFound) {
                throw new BusinessException(
                    String.format("与预订信息不符，无法办理入住。预订主住客：%s，预订手机号需与入住住客之一匹配",
                        activeBooking.getMainGuestName()));
            }
            
            // 4.3 验证通过，更新预订状态
            activeBooking.setStatus("CHECKED_IN");
            bookingMapper.updateById(activeBooking);
            log.info("预订状态已更新为CHECKED_IN，bookingId={}", activeBooking.getBookingId());
        } else {
            // 4.4 Walk-in入住：检查预期退房时间是否与未来预订冲突
            log.info("未找到今日预订，进入Walk-in入住检查流程");
            
            LocalDateTime expectedCheckout = request.getExpectedCheckout() != null 
                    ? request.getExpectedCheckout() 
                    : now.plusDays(1);
            
            log.info("Walk-in预期退房时间: {}", expectedCheckout);
            
            // 查询该房间未来的预订记录（PENDING或CONFIRMED状态）
            LambdaQueryWrapper<Booking> futureBookingWrapper = new LambdaQueryWrapper<>();
            futureBookingWrapper.eq(Booking::getRoomId, room.getRoomId())
                    .in(Booking::getStatus, "PENDING", "CONFIRMED")
                    .gt(Booking::getPlannedCheckin, now); // 计划入住时间在当前时间之后
            
            List<Booking> futureBookings = bookingMapper.selectList(futureBookingWrapper);
            log.info("查询到未来预订数量: {}", futureBookings.size());
            
            for (Booking futureBooking : futureBookings) {
                log.info("检查预订冲突: bookingId={}, plannedCheckin={}, plannedCheckout={}", 
                        futureBooking.getBookingId(), 
                        futureBooking.getPlannedCheckin(), 
                        futureBooking.getPlannedCheckout());
                
                // 检查Walk-in的退房时间是否超过预订的入住时间
                if (expectedCheckout.isAfter(futureBooking.getPlannedCheckin())) {
                    log.warn("发现Walk-in时间冲突! expectedCheckout={}, futureBookingCheckin={}", 
                            expectedCheckout, futureBooking.getPlannedCheckin());
                    throw new BusinessException(String.format(
                            "房间部分时段已被预订。预订入住时间：%s，请选择在此之前退房",
                            futureBooking.getPlannedCheckin().toLocalDate()));
                } else {
                    log.info("无时间冲突: expectedCheckout={} 早于 plannedCheckin={}", 
                            expectedCheckout, futureBooking.getPlannedCheckin());
                }
            }
            
            log.info("Walk-in时间冲突检查完成，无冲突");
        }

        // 5. 处理每个住客的注册和入住
        List<CheckInResponse.GuestDetail> guestDetails = new ArrayList<>();
        LocalDateTime expectedCheckout = request.getExpectedCheckout() != null 
                ? request.getExpectedCheckout() 
                : now.plusDays(1); // 默认次日12点

        String roomAuthToken = null; // 第一个住客获得房间权限Token

        for (int i = 0; i < request.getGuests().size(); i++) {
            CheckInRequest.GuestInfo guestInfo = request.getGuests().get(i);
            
            // 5.1 查询或创建用户账号
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getPhone, guestInfo.getPhone());
            User user = userMapper.selectOne(userWrapper);
            
            boolean isNewUser = false;
            if (user == null) {
                // 自动注册新用户
                user = new User();
                user.setUsername(guestInfo.getPhone());
                user.setPhone(guestInfo.getPhone());
                user.setPasswordHash(DEFAULT_PASSWORD_HASH);
                user.setUserType("GUEST");
                user.setStatus("ACTIVE");
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                userMapper.insert(user);
                isNewUser = true;
                log.info("自动创建用户账号: userId={}, phone={}, 默认密码={}", 
                        user.getUserId(), guestInfo.getPhone(), DEFAULT_PASSWORD);
            }

            // 5.2 查询或创建Guest记录
            LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
            guestWrapper.eq(Guest::getUserId, user.getUserId());
            Guest guest = guestMapper.selectOne(guestWrapper);

            if (guest == null) {
                // 创建新Guest记录
                guest = new Guest();
                guest.setUserId(user.getUserId());
                guest.setRealName(guestInfo.getRealName());
                guest.setIdentityCard(aesUtil.encrypt(guestInfo.getIdCard())); // AES加密
                guest.setMemberLevel("BRONZE");
                guest.setExperiencePoints(0);
                guest.setCurrentPoints(0);
                guest.setTotalCheckinNights(0);
                guestMapper.insert(guest);
                log.info("创建Guest记录: guestId={}, userId={}, realName={}", 
                        guest.getGuestId(), user.getUserId(), guestInfo.getRealName());
            } else {
                // 更新实名信息（如果之前未绑定）
                if (guest.getRealName() == null || guest.getIdentityCard() == null) {
                    guest.setRealName(guestInfo.getRealName());
                    guest.setIdentityCard(aesUtil.encrypt(guestInfo.getIdCard()));
                    guestMapper.updateById(guest);
                    log.info("更新Guest实名信息: guestId={}, realName={}", 
                            guest.getGuestId(), guestInfo.getRealName());
                }
            }

            // 5.3 检查是否已有未退房的入住记录
            LambdaQueryWrapper<CheckInRecord> activeRecordWrapper = new LambdaQueryWrapper<>();
            activeRecordWrapper.eq(CheckInRecord::getGuestId, guest.getGuestId())
                               .isNull(CheckInRecord::getActualCheckout);
            Long activeCount = checkInRecordMapper.selectCount(activeRecordWrapper);
            if (activeCount > 0) {
                throw new BusinessException(String.format(
                        "住客 %s(%s) 当前已有未退房的入住记录，请先办理退房", 
                        guestInfo.getRealName(), guestInfo.getPhone()));
            }

            // 5.4 创建入住记录
            CheckInRecord record = new CheckInRecord();
            record.setGuestId(guest.getGuestId());
            record.setRoomId(room.getRoomId());
            record.setActualCheckin(now);
            record.setExpectedCheckout(expectedCheckout);
            record.setIsGamingAuthActive(true); // 启用游戏权限
            record.setPaymentStatus("UNPAID"); // 待支付
            checkInRecordMapper.insert(record);

            // 5.5 第一个住客生成房间权限Token
            if (i == 0) {
                roomAuthToken = jwtUtil.generateRoomAuthToken(
                        user.getUserId(), 
                        room.getRoomId(), 
                        record.getRecordId());
                log.info("生成房间权限Token: userId={}, roomId={}, recordId={}", 
                        user.getUserId(), room.getRoomId(), record.getRecordId());
            }

            // 5.6 添加到响应列表
            CheckInResponse.GuestDetail detail = new CheckInResponse.GuestDetail();
            detail.setRecordId(record.getRecordId());
            detail.setGuestId(guest.getGuestId());
            detail.setUserId(user.getUserId());
            detail.setRealName(guestInfo.getRealName());
            detail.setPhone(guestInfo.getPhone());
            detail.setIsNewUser(isNewUser);
            detail.setRoomAuthToken(i == 0 ? roomAuthToken : null); // 仅第一个住客有Token
            guestDetails.add(detail);
        }

        // 6. 更新房间状态
        room.setStatus("OCCUPIED");
        room.setCurrentOccupancy(guestCount);
        roomMapper.updateById(room);
        log.info("更新房间状态: roomNo={}, status=OCCUPIED, currentOccupancy={}", 
                room.getRoomNo(), guestCount);

        // 7. 构造响应
        CheckInResponse response = new CheckInResponse();
        response.setRoomNo(room.getRoomNo());
        response.setRoomId(room.getRoomId());
        response.setCheckInTime(now);
        response.setExpectedCheckout(expectedCheckout);
        response.setPricePerHour(room.getPricePerHour());
        response.setCurrentOccupancy(guestCount);
        response.setMaxOccupancy(room.getMaxOccupancy());
        response.setGuests(guestDetails);

        log.info("入住登记完成: roomNo={}, 入住人数={}, 新注册用户数={}", 
                room.getRoomNo(), guestCount, 
                guestDetails.stream().filter(CheckInResponse.GuestDetail::getIsNewUser).count());
        
        return response;
    }
}

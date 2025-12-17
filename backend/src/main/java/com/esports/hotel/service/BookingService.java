package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.BookingRequest;
import com.esports.hotel.dto.BookingResponse;
import com.esports.hotel.entity.Booking;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.Room;
import com.esports.hotel.entity.User;
import com.esports.hotel.mapper.BookingMapper;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.RoomMapper;
import com.esports.hotel.mapper.UserMapper;
import com.esports.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预订服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingMapper bookingMapper;
    private final RoomMapper roomMapper;
    private final GuestMapper guestMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 创建预订
     */
    @Transactional(rollbackFor = Exception.class)
    public BookingResponse createBooking(BookingRequest request, String token) {
        // 1. 从token获取用户信息（去除Bearer前缀）
        log.info("收到的token: [{}], 长度: {}", token, token.length());
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        log.info("处理后的jwtToken: [{}], 长度: {}", jwtToken, jwtToken.length());
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 获取Guest信息
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getUserId, userId);
        Guest guest = guestMapper.selectOne(guestWrapper);
        if (guest == null) {
            throw new BusinessException("住客信息不存在");
        }

        // 3. 检查是否已有活跃预订（PENDING或CONFIRMED状态）
        LambdaQueryWrapper<Booking> activeBookingWrapper = new LambdaQueryWrapper<>();
        activeBookingWrapper.eq(Booking::getGuestId, guest.getGuestId())
                .in(Booking::getStatus, "PENDING", "CONFIRMED");
        Long activeBookingCount = bookingMapper.selectCount(activeBookingWrapper);
        if (activeBookingCount > 0) {
            throw new BusinessException("您已有待入住的预订，请先完成入住或取消后再预订");
        }

        // 4. 验证房间是否存在
        Room room = roomMapper.selectById(request.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        // 5. 验证时间合理性
        if (request.getPlannedCheckout().isBefore(request.getPlannedCheckin())) {
            throw new BusinessException("退房时间不能早于入住时间");
        }

        // 6. 检查房间在该时段是否可预订
        LocalDateTime checkinDateTime = request.getPlannedCheckin().atStartOfDay();
        LocalDateTime checkoutDateTime = request.getPlannedCheckout().atStartOfDay();
        
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(Booking::getRoomId, request.getRoomId())
                .in(Booking::getStatus, "PENDING", "CONFIRMED")
                .and(wrapper -> wrapper
                        .between(Booking::getPlannedCheckin, checkinDateTime, checkoutDateTime)
                        .or()
                        .between(Booking::getPlannedCheckout, checkinDateTime, checkoutDateTime)
                        .or()
                        .and(w -> w
                                .le(Booking::getPlannedCheckin, checkinDateTime)
                                .ge(Booking::getPlannedCheckout, checkoutDateTime)
                        )
                );
        Long conflictCount = bookingMapper.selectCount(bookingWrapper);
        if (conflictCount > 0) {
            throw new BusinessException("该时段房间已被预订");
        }

        // 7. 创建预订记录（在insert前设置所有字段）
        Booking booking = new Booking();
        booking.setGuestId(guest.getGuestId());
        booking.setRoomId(request.getRoomId());
        // 将LocalDate转换为LocalDateTime（设置为当天00:00:00）
        booking.setPlannedCheckin(request.getPlannedCheckin().atStartOfDay());
        booking.setPlannedCheckout(request.getPlannedCheckout().atStartOfDay());
        booking.setStatus("CONFIRMED");
        booking.setDepositAmount(BigDecimal.ZERO); // 押金暂时为0
        booking.setDiscountRate(BigDecimal.valueOf(1.00));
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setBookingTime(LocalDateTime.now());
        // 设置主住客信息（用于入住验证）
        booking.setMainGuestName(request.getMainGuestName());
        booking.setContactPhone(request.getContactPhone());
        bookingMapper.insert(booking);

        log.info("预订创建成功: bookingId={}, guestId={}, roomId={}, mainGuestName={}, contactPhone={}", 
                booking.getBookingId(), guest.getGuestId(), request.getRoomId(), 
                request.getMainGuestName(), request.getContactPhone());

        // 8. 构造响应
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setRoomNo(room.getRoomNo());
        response.setRoomId(room.getRoomId());
        response.setRoomType(room.getRoomType());
        response.setGuestId(guest.getGuestId());
        response.setMainGuestName(request.getMainGuestName());
        response.setBookingTime(booking.getBookingTime());
        response.setPlannedCheckin(booking.getPlannedCheckin());
        response.setPlannedCheckout(booking.getPlannedCheckout());
        response.setStatus(booking.getStatus());
        response.setDepositAmount(booking.getDepositAmount());
        response.setSpecialRequests(booking.getSpecialRequests());

        return response;
    }

    /**
     * 获取用户的预订列表
     */
    public List<BookingResponse> getMyBookings(String token) {
        // 1. 从 token获取用户信息（去除Bearer前缀）
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);

        // 2. 获取Guest信息
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getUserId, userId);
        Guest guest = guestMapper.selectOne(guestWrapper);
        if (guest == null) {
            throw new BusinessException("住客信息不存在");
        }

        // 3. 查询预订记录
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(Booking::getGuestId, guest.getGuestId())
                .orderByDesc(Booking::getBookingTime);
        List<Booking> bookings = bookingMapper.selectList(bookingWrapper);

        // 4. 构造响应
        return bookings.stream().map(booking -> {
            Room room = roomMapper.selectById(booking.getRoomId());
            BookingResponse response = new BookingResponse();
            response.setBookingId(booking.getBookingId());
            response.setRoomNo(room != null ? room.getRoomNo() : null);
            response.setRoomId(booking.getRoomId());
            response.setRoomType(room != null ? room.getRoomType() : null);
            response.setGuestId(booking.getGuestId());
            response.setMainGuestName(booking.getMainGuestName());
            response.setBookingTime(booking.getBookingTime());
            response.setPlannedCheckin(booking.getPlannedCheckin());
            response.setPlannedCheckout(booking.getPlannedCheckout());
            response.setStatus(booking.getStatus());
            response.setDepositAmount(booking.getDepositAmount());
            response.setSpecialRequests(booking.getSpecialRequests());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 取消预订
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId, String token) {
        // 1. 验证预订是否存在
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        // 2. 验证是否是本人的预订（去除Bearer前缀）
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getUserId, userId);
        Guest guest = guestMapper.selectOne(guestWrapper);
        if (guest == null || !guest.getGuestId().equals(booking.getGuestId())) {
            throw new BusinessException("无权取消此预订");
        }

        // 3. 验证状态是否可取消
        if ("CHECKED_IN".equals(booking.getStatus())) {
            throw new BusinessException("已入住的预订无法取消");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BusinessException("预订已取消");
        }

        // 4. 更新状态
        booking.setStatus("CANCELLED");
        bookingMapper.updateById(booking);

        log.info("预订已取消: bookingId={}, guestId={}", bookingId, guest.getGuestId());
    }

    /**
     * 根据手机号查询今日预订（用于前台办理预订入住）
     */
    public BookingResponse getBookingByPhone(String phone) {
        // 1. 根据手机号查找用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            throw new BusinessException("未找到该手机号对应的用户");
        }

        // 2. 获取Guest信息
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getUserId, user.getUserId());
        Guest guest = guestMapper.selectOne(guestWrapper);
        if (guest == null) {
            throw new BusinessException("该用户不是住客");
        }

        // 3. 查询今日的CONFIRMED状态预订
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        
        LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(Booking::getGuestId, guest.getGuestId())
                .eq(Booking::getStatus, "CONFIRMED")
                .ge(Booking::getPlannedCheckin, todayStart)
                .lt(Booking::getPlannedCheckin, todayEnd)
                .orderByDesc(Booking::getBookingTime)
                .last("LIMIT 1");
        
        Booking booking = bookingMapper.selectOne(bookingWrapper);
        if (booking == null) {
            throw new BusinessException("未找到该手机号今日的预订记录");
        }

        // 4. 构造响应
        Room room = roomMapper.selectById(booking.getRoomId());
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setRoomNo(room != null ? room.getRoomNo() : null);
        response.setRoomId(booking.getRoomId());
        response.setRoomType(room != null ? room.getRoomType() : null);
        response.setGuestId(booking.getGuestId());
        response.setMainGuestName(booking.getMainGuestName());
        response.setBookingTime(booking.getBookingTime());
        response.setPlannedCheckin(booking.getPlannedCheckin());
        response.setPlannedCheckout(booking.getPlannedCheckout());
        response.setStatus(booking.getStatus());
        response.setDepositAmount(booking.getDepositAmount());
        response.setSpecialRequests(booking.getSpecialRequests());

        log.info("根据手机号查询到预订: phone={}, bookingId={}, roomId={}", phone, booking.getBookingId(), booking.getRoomId());
        return response;
    }
}

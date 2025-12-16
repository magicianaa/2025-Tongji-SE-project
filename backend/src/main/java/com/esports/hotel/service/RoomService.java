package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.common.ResultCode;
import com.esports.hotel.dto.CheckInRequest;
import com.esports.hotel.dto.CheckInResponse;
import com.esports.hotel.dto.CheckOutResponse;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.Room;
import com.esports.hotel.mapper.CheckInRecordMapper;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.RoomMapper;
import com.esports.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 客房管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;
    private final GuestMapper guestMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final JwtUtil jwtUtil;

    /**
     * 办理入住
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckInResponse checkIn(CheckInRequest request) {
        // 1. 验证房间状态
        Room room = roomMapper.selectById(request.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        }
        if (!"VACANT".equals(room.getStatus())) {
            throw new BusinessException(ResultCode.ROOM_NOT_AVAILABLE);
        }

        // 2. 验证住客信息
        Guest guest = guestMapper.selectById(request.getGuestId());
        if (guest == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 检查是否已有未退房的记录
        LambdaQueryWrapper<CheckInRecord> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(CheckInRecord::getGuestId, request.getGuestId())
                      .isNull(CheckInRecord::getActualCheckout);
        if (checkInRecordMapper.selectCount(existingWrapper) > 0) {
            throw new BusinessException(ResultCode.CHECKIN_ALREADY_EXISTS.getCode(), "您还有未退房的订单");
        }

        // 4. 更新住客实名信息（如果提供）
        if (request.getRealName() != null) {
            guest.setRealName(request.getRealName());
        }
        if (request.getIdentityCard() != null) {
            // TODO: 实际生产环境需使用 AES 加密
            guest.setIdentityCard(request.getIdentityCard());
        }
        guestMapper.updateById(guest);

        // 5. 创建入住记录
        CheckInRecord record = new CheckInRecord();
        record.setBookingId(request.getBookingId());
        record.setRoomId(request.getRoomId());
        record.setGuestId(request.getGuestId());
        record.setExpectedCheckout(request.getExpectedCheckout());
        record.setIsGamingAuthActive(true);  // 开启客房权限
        record.setRoomFee(BigDecimal.ZERO);
        record.setDamageCompensation(BigDecimal.ZERO);
        record.setPointsDeduction(0);
        record.setFinalAmount(BigDecimal.ZERO);
        record.setPaymentStatus("UNPAID");
        record.setNotes(request.getSpecialRequests());
        checkInRecordMapper.insert(record);

        // 6. 更新房态为"入住中"
        room.setStatus("OCCUPIED");
        roomMapper.updateById(room);

        // 7. 生成客房权限 Token（二次鉴权）
        String roomAuthToken = jwtUtil.generateRoomAuthToken(
                guest.getUserId(), 
                record.getRecordId(), 
                room.getRoomId()
        );

        // 8. 构造响应
        CheckInResponse response = new CheckInResponse();
        response.setRecordId(record.getRecordId());
        response.setRoomNo(room.getRoomNo());
        response.setRoomId(room.getRoomId());
        response.setActualCheckin(record.getActualCheckin());
        response.setExpectedCheckout(record.getExpectedCheckout());
        response.setPricePerHour(room.getPricePerHour());
        response.setRoomAuthToken(roomAuthToken);
        response.setMessage("入住成功！请使用 Room-Auth-Token 访问客房服务");

        log.info("办理入住成功: recordId={}, guestId={}, roomNo={}", 
                 record.getRecordId(), request.getGuestId(), room.getRoomNo());
        
        return response;
    }

    /**
     * 办理退房
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckOutResponse checkOut(Long recordId, String paymentMethod) {
        // 1. 查询入住记录
        CheckInRecord record = checkInRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "入住记录不存在");
        }
        if (record.getActualCheckout() != null) {
            throw new BusinessException("该订单已退房");
        }

        // 2. 计算房费
        LocalDateTime checkInTime = record.getActualCheckin();
        LocalDateTime checkOutTime = LocalDateTime.now();
        long stayHours = Duration.between(checkInTime, checkOutTime).toHours();
        if (stayHours < 1) {
            stayHours = 1; // 最少按1小时计费
        }

        Room room = roomMapper.selectById(record.getRoomId());
        BigDecimal roomFee = room.getPricePerHour()
                .multiply(BigDecimal.valueOf(stayHours))
                .setScale(2, RoundingMode.HALF_UP);

        // 3. 汇总 POS 挂账金额（TODO: 实际需查询 POS 订单表）
        BigDecimal posCharges = BigDecimal.ZERO;

        // 4. 计算最终金额
        BigDecimal finalAmount = roomFee
                .add(posCharges)
                .add(record.getDamageCompensation())
                .subtract(BigDecimal.valueOf(record.getPointsDeduction() / 100.0));

        // 5. 更新入住记录
        record.setActualCheckout(checkOutTime);
        record.setRoomFee(roomFee);
        record.setFinalAmount(finalAmount);
        record.setPaymentStatus("PAID");
        record.setPaymentMethod(paymentMethod);
        record.setIsGamingAuthActive(false);  // 立即回收客房权限
        checkInRecordMapper.updateById(record);

        // 6. 更新房态为"脏房"
        room.setStatus("DIRTY");
        roomMapper.updateById(room);

        // 7. 更新住客累计入住天数和积分
        Guest guest = guestMapper.selectById(record.getGuestId());
        guest.setTotalCheckinNights(guest.getTotalCheckinNights() + 1);
        // 赠送积分：每消费1元赠送10积分
        int earnedPoints = finalAmount.multiply(BigDecimal.valueOf(10)).intValue();
        guest.setCurrentPoints(guest.getCurrentPoints() + earnedPoints);
        guestMapper.updateById(guest);

        // 8. 构造账单响应
        CheckOutResponse response = new CheckOutResponse();
        response.setRecordId(recordId);
        response.setRoomNo(room.getRoomNo());
        response.setStayHours(stayHours);
        response.setRoomFee(roomFee);
        response.setPosCharges(posCharges);
        response.setDamageCompensation(record.getDamageCompensation());
        response.setPointsDeduction(BigDecimal.valueOf(record.getPointsDeduction() / 100.0));
        response.setFinalAmount(finalAmount);
        response.setPaymentMethod(paymentMethod);

        // 9. 生成账单明细
        List<CheckOutResponse.BillItem> billItems = new ArrayList<>();
        billItems.add(new CheckOutResponse.BillItem("房费", roomFee, "ROOM_FEE"));
        if (posCharges.compareTo(BigDecimal.ZERO) > 0) {
            billItems.add(new CheckOutResponse.BillItem("POS消费", posCharges, "POS"));
        }
        if (record.getDamageCompensation().compareTo(BigDecimal.ZERO) > 0) {
            billItems.add(new CheckOutResponse.BillItem("赔偿金", record.getDamageCompensation(), "DAMAGE"));
        }
        response.setBillItems(billItems);

        log.info("办理退房成功: recordId={}, finalAmount={}, earnedPoints={}", 
                 recordId, finalAmount, earnedPoints);

        return response;
    }

    /**
     * 查询所有房间（房态管理）
     */
    public List<Room> getAllRooms() {
        return roomMapper.selectList(null);
    }

    /**
     * 查询空闲房间
     */
    public List<Room> getVacantRooms() {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getStatus, "VACANT");
        return roomMapper.selectList(wrapper);
    }

    /**
     * 根据状态查询房间
     */
    public List<Room> getRoomsByStatus(String status) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getStatus, status);
        return roomMapper.selectList(wrapper);
    }

    /**
     * 获取所有活跃的入住记录（未退房）
     */
    public List<CheckInRecord> getActiveCheckInRecords() {
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(CheckInRecord::getActualCheckout);
        return checkInRecordMapper.selectList(wrapper);
    }
}

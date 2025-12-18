package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.common.ResultCode;
import com.esports.hotel.dto.CheckInRequest;
import com.esports.hotel.dto.CheckInResponse;
import com.esports.hotel.dto.CheckOutResponse;
import com.esports.hotel.dto.RoomVO;
import com.esports.hotel.entity.Booking;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.Room;
import com.esports.hotel.mapper.BookingMapper;
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
    private final BookingMapper bookingMapper;
    private final JwtUtil jwtUtil;
    private final PointsService pointsService;

    /**
     * 办理入住 - 此方法已废弃
     * 新的多人入住功能请使用 CheckInService.checkIn()
     * 
     * @deprecated 已由 CheckInService.checkIn() 替代
     */
    /*
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public CheckInResponse checkIn(CheckInRequest request) {
        // 此方法已被 CheckInService 的多人入住功能替代
        // 保留注释以供参考
    }
    */

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
        
        // 按天计费：进1制，不满1天按1天计算
        long stayDays = (stayHours + 23) / 24; // 向上取整
        if (stayDays < 1) {
            stayDays = 1;
        }

        Room room = roomMapper.selectById(record.getRoomId());
        BigDecimal roomFee = room.getPricePerDay()
                .multiply(BigDecimal.valueOf(stayDays))
                .setScale(2, RoundingMode.HALF_UP);

        // 3. 汇总 POS 挂账金额（TODO: 实际需查询 POS 订单表）
        BigDecimal posCharges = BigDecimal.ZERO;

        // 4. 获取住客信息，应用会员折扣
        Guest guest = guestMapper.selectById(record.getGuestId());
        double discountRate = pointsService.getMemberDiscountRate(guest.getMemberLevel());
        
        // 5. 计算最终金额（应用会员折扣）
        BigDecimal subtotal = roomFee
                .add(posCharges)
                .add(record.getDamageCompensation());
        BigDecimal discountAmount = subtotal.multiply(BigDecimal.valueOf(1 - discountRate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = subtotal.multiply(BigDecimal.valueOf(discountRate))
                .subtract(BigDecimal.valueOf(record.getPointsDeduction() / 100.0))
                .setScale(2, RoundingMode.HALF_UP);

        // 6. 更新入住记录
        record.setActualCheckout(checkOutTime);
        record.setRoomFee(roomFee);
        record.setFinalAmount(finalAmount);
        record.setPaymentStatus("PAID");
        record.setPaymentMethod(paymentMethod);
        record.setIsGamingAuthActive(false);  // 立即回收客房权限
        checkInRecordMapper.updateById(record);

        // 7. 更新房态为"待清洁"，并重置入住人数
        room.setStatus("CLEANING");
        room.setCurrentOccupancy(0);
        roomMapper.updateById(room);

        // 8. 更新住客累计入住天数
        guest.setTotalCheckinNights(guest.getTotalCheckinNights() + (int) stayDays);
        guestMapper.updateById(guest);
        
        // 9. 发放退房积分奖励（消费金额等值积分，自动升级会员等级）
        int earnedPoints = finalAmount.intValue();
        if (earnedPoints > 0) {
            pointsService.addPoints(
                record.getGuestId(),
                earnedPoints,
                "ADMIN_ADJUST",
                recordId,
                "退房结算奖励积分"
            );
        }

        // 10. 构造账单响应
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

        // 11. 生成账单明细
        List<CheckOutResponse.BillItem> billItems = new ArrayList<>();
        billItems.add(new CheckOutResponse.BillItem("房费", roomFee, "ROOM_FEE"));
        if (posCharges.compareTo(BigDecimal.ZERO) > 0) {
            billItems.add(new CheckOutResponse.BillItem("POS消费", posCharges, "POS"));
        }
        if (record.getDamageCompensation().compareTo(BigDecimal.ZERO) > 0) {
            billItems.add(new CheckOutResponse.BillItem("赔偿金", record.getDamageCompensation(), "DAMAGE"));
        }
        if (discountRate < 1.0) {
            billItems.add(new CheckOutResponse.BillItem("会员折扣(" + guest.getMemberLevel() + ")", 
                discountAmount.negate(), "DISCOUNT"));
        }
        response.setBillItems(billItems);

        log.info("办理退房成功: recordId={}, finalAmount={}, earnedPoints={}, memberLevel={}, discount={}%", 
                 recordId, finalAmount, earnedPoints, guest.getMemberLevel(), (int)((1 - discountRate) * 100));

        return response;
    }

    /**
     * 查询所有房间（房态管理）
     */
    public List<Room> getAllRooms() {
        return roomMapper.selectList(null);
    }

    /**
     * 获取包含预订信息的房间列表
     */
    public List<RoomVO> getAllRoomsWithBooking() {
        List<Room> rooms = roomMapper.selectList(null);
        LocalDateTime now = LocalDateTime.now();
        
        return rooms.stream().map(room -> {
            RoomVO vo = new RoomVO();
            vo.setRoomId(room.getRoomId());
            vo.setRoomNo(room.getRoomNo());
            vo.setRoomType(room.getRoomType());
            vo.setFloor(room.getFloor());
            vo.setStatus(room.getStatus());
            vo.setPricePerDay(room.getPricePerDay());
            vo.setMaxOccupancy(room.getMaxOccupancy());
            vo.setCurrentOccupancy(room.getCurrentOccupancy());
            vo.setFacilityConfig(room.getFacilityConfig());
            vo.setIsPremium(room.getIsPremium());
            
            // 查询是否有有效预订（当前时间段内的预订）
            Booking booking = bookingMapper.selectCurrentBookingForRoom(room.getRoomId());
            
            if (booking != null) {
                vo.setHasBooking(true);
                vo.setBookingId(booking.getBookingId());
                vo.setBookingStatus(booking.getStatus());
            } else {
                vo.setHasBooking(false);
            }
            
            return vo;
        }).toList();
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

    /**
     * 打扫完毕 - CLEANING -> VACANT
     */
    @Transactional(rollbackFor = Exception.class)
    public void markCleaned(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        }
        if (!"CLEANING".equals(room.getStatus())) {
            throw new BusinessException("当前房间状态不是待清洁，无法标记为已打扫");
        }
        
        room.setStatus("VACANT");
        room.setCurrentOccupancy(0);
        roomMapper.updateById(room);
        log.info("房间打扫完毕: roomNo={}, {} -> VACANT", room.getRoomNo(), "CLEANING");
    }

    /**
     * 维修完成 - MAINTENANCE -> VACANT
     */
    @Transactional(rollbackFor = Exception.class)
    public void markRepaired(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        }
        if (!"MAINTENANCE".equals(room.getStatus())) {
            throw new BusinessException("当前房间状态不是维修中，无法标记为已维修");
        }
        
        room.setStatus("VACANT");
        room.setCurrentOccupancy(0);
        roomMapper.updateById(room);
        log.info("房间维修完成: roomNo={}, MAINTENANCE -> VACANT", room.getRoomNo());
    }

    /**
     * 设置维修中 - VACANT/CLEANING -> MAINTENANCE
     */
    @Transactional(rollbackFor = Exception.class)
    public void markMaintenance(Long roomId, String reason) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        }
        if ("OCCUPIED".equals(room.getStatus())) {
            throw new BusinessException("房间当前有客人入住，无法设置为维修中");
        }
        
        room.setStatus("MAINTENANCE");
        room.setCurrentOccupancy(0);
        roomMapper.updateById(room);
        log.info("房间设置为维修中: roomNo={}, reason={}", room.getRoomNo(), reason);
    }
}

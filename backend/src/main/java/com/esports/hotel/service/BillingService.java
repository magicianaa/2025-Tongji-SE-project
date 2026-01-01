package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.BillDetailDTO;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.PosOrder;
import com.esports.hotel.entity.Room;
import com.esports.hotel.entity.User;
import com.esports.hotel.mapper.CheckInRecordMapper;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.PosOrderMapper;
import com.esports.hotel.mapper.RoomMapper;
import com.esports.hotel.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
    
    private final CheckInRecordMapper recordMapper;
    private final RoomMapper roomMapper;
    private final PosOrderMapper posOrderMapper;
    private final UserMapper userMapper;
    private final GuestMapper guestMapper;
    private final PointsService pointsService;
    
    /**
     * 获取账单详情
     */
    public BillDetailDTO getBillDetail(Long recordId) {
        // 1. 查询入住记录
        CheckInRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("入住记录不存在");
        }
        
        if (record.getActualCheckout() != null) {
            throw new BusinessException("该房间已退房");
        }
        
        // 2. 查询房间信息
        Room room = roomMapper.selectById(record.getRoomId());
        if (room == null) {
            throw new BusinessException("房间信息不存在");
        }
        
        // 3. 计算房费
        LocalDateTime checkInTime = record.getActualCheckin();
        LocalDateTime currentTime = LocalDateTime.now();
        long stayHours = Duration.between(checkInTime, currentTime).toHours();
        
        // 按天计费：进1制
        long stayDays = (stayHours + 23) / 24;
        if (stayDays < 1) {
            stayDays = 1;
        }
        
        BigDecimal roomFee = room.getPricePerDay()
                .multiply(BigDecimal.valueOf(stayDays));
        
        // 4. 查询该房间所有住客的POS订单
        List<CheckInRecord> allRecords = recordMapper.selectList(
            new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getRoomId, record.getRoomId())
                .isNull(CheckInRecord::getActualCheckout)
        );
        
        List<Long> recordIds = allRecords.stream()
                .map(CheckInRecord::getRecordId)
                .collect(Collectors.toList());
        
        List<PosOrder> posOrders = posOrderMapper.selectList(
            new LambdaQueryWrapper<PosOrder>()
                .in(PosOrder::getRecordId, recordIds)
                .ne(PosOrder::getStatus, "CANCELLED")
        );
        
        // 5. 构建POS订单摘要
        List<BillDetailDTO.PosOrderSummary> orderSummaries = posOrders.stream()
                .map(order -> {
                    CheckInRecord orderRecord = recordMapper.selectById(order.getRecordId());
                    User user = userMapper.selectById(orderRecord.getGuestId());
                    
                    BillDetailDTO.PosOrderSummary summary = new BillDetailDTO.PosOrderSummary();
                    summary.setOrderId(order.getOrderId());
                    summary.setOrderNo(order.getOrderNo());
                    summary.setOrderType(order.getOrderType());
                    summary.setCreateTime(order.getCreateTime());
                    summary.setTotalAmount(order.getTotalAmount());
                    summary.setStatus(order.getStatus());
                    summary.setGuestName(user.getUsername());
                    summary.setPhone(user.getPhone());
                    return summary;
                })
                .collect(Collectors.toList());
        
        BigDecimal posTotal = posOrders.stream()
                .map(PosOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 6. 计算会员折扣：查询该房间所有住客中最高的会员等级
        String highestMemberLevel = "BRONZE";
        int highestLevelRank = 0;
        
        for (CheckInRecord r : allRecords) {
            Guest guest = guestMapper.selectOne(
                new LambdaQueryWrapper<Guest>().eq(Guest::getUserId, r.getGuestId())
            );
            if (guest != null && guest.getMemberLevel() != null) {
                int levelRank = getMemberLevelRank(guest.getMemberLevel());
                if (levelRank > highestLevelRank) {
                    highestLevelRank = levelRank;
                    highestMemberLevel = guest.getMemberLevel();
                }
            }
        }
        
        // 7. 根据会员等级计算折扣率
        BigDecimal discountRate = getDiscountRate(highestMemberLevel);
        
        // 8. 计算总金额（折扣前）
        BigDecimal totalAmountBeforeDiscount = roomFee.add(posTotal);
        
        // 9. 计算折扣后金额
        BigDecimal totalAmount = totalAmountBeforeDiscount.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal paidAmount = record.getRoomFee() != null ? record.getRoomFee() : BigDecimal.ZERO;
        BigDecimal unpaidAmount = totalAmount.subtract(paidAmount);
        
        // 防止待支付金额为负数（已支付金额大于折扣后金额时）
        if (unpaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            unpaidAmount = BigDecimal.ZERO;
        }
        
        // 10. 构建响应
        BillDetailDTO dto = new BillDetailDTO();
        dto.setRecordId(recordId);
        dto.setRoomNo(room.getRoomNo());
        dto.setRoomType(room.getRoomType());
        dto.setCheckInTime(checkInTime);
        dto.setCurrentTime(currentTime);
        dto.setStayDays(stayDays);
        dto.setPricePerDay(room.getPricePerDay());
        dto.setRoomFee(roomFee);
        dto.setPosOrders(orderSummaries);
        dto.setPosTotal(posTotal);
        dto.setTotalAmount(totalAmount);
        dto.setPaidAmount(paidAmount);
        dto.setUnpaidAmount(unpaidAmount);
        
        return dto;
    }
    
    /**
     * 账单清付（仅结算费用，不退房）
     * 房间内任意住客清付后，该房间所有住客的待支付金额都归零
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleBill(Long recordId, String paymentMethod) {
        // 1. 获取账单详情（已包含折扣）
        BillDetailDTO billDetail = getBillDetail(recordId);
        
        // 2. 查询该房间所有未退房的入住记录
        CheckInRecord record = recordMapper.selectById(recordId);
        List<CheckInRecord> allRecords = recordMapper.selectList(
            new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getRoomId, record.getRoomId())
                .isNull(CheckInRecord::getActualCheckout)
        );
        
        // 3. 计算每个住客应获得的积分和经验值（折扣前金额÷人数，向上取整）
        Room room = roomMapper.selectById(record.getRoomId());
        int occupancy = allRecords.size();
        
        // 计算折扣前总金额
        LocalDateTime checkInTime = record.getActualCheckin();
        LocalDateTime currentTime = LocalDateTime.now();
        long stayHours = Duration.between(checkInTime, currentTime).toHours();
        long stayDays = (stayHours + 23) / 24;
        if (stayDays < 1) {
            stayDays = 1;
        }
        BigDecimal roomFee = room.getPricePerDay().multiply(BigDecimal.valueOf(stayDays));
        
        List<Long> recordIds = allRecords.stream()
                .map(CheckInRecord::getRecordId)
                .collect(Collectors.toList());
        List<PosOrder> posOrders = posOrderMapper.selectList(
            new LambdaQueryWrapper<PosOrder>()
                .in(PosOrder::getRecordId, recordIds)
                .ne(PosOrder::getStatus, "CANCELLED")
        );
        BigDecimal posTotal = posOrders.stream()
                .map(PosOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmountBeforeDiscount = roomFee.add(posTotal);
        
        // 每个住客获得的积分和经验值（向上取整）
        int pointsPerGuest = totalAmountBeforeDiscount
                .divide(BigDecimal.valueOf(occupancy), 0, RoundingMode.UP)
                .intValue();
        
        // 4. 更新该房间所有住客的费用信息，并增加积分和经验值
        for (CheckInRecord r : allRecords) {
            r.setRoomFee(billDetail.getTotalAmount());
            r.setFinalAmount(billDetail.getTotalAmount());
            r.setPaymentStatus("PAID");
            r.setPaymentMethod(paymentMethod);
            recordMapper.updateById(r);
            
            // 为每个住客增加积分和经验值
            Guest guest = guestMapper.selectById(r.getGuestId());
            if (guest != null) {
                try {
                    pointsService.addPoints(
                        guest.getGuestId(), 
                        pointsPerGuest, 
                        "ADMIN_ADJUST", 
                        null, 
                        "账单清付奖励"
                    );
                    log.info("住客 {} 获得清付奖励: {} 积分/经验", guest.getGuestId(), pointsPerGuest);
                } catch (Exception e) {
                    log.error("为住客 {} 添加积分失败，但不影响账单结算: {}", guest.getGuestId(), e.getMessage());
                }
            } else {
                log.warn("未找到guestId={}的住客信息，跳过积分奖励", r.getGuestId());
            }
        }
        
        log.info("账单清付成功: roomId={}, recordCount={}, totalAmount={}, paymentMethod={}, pointsPerGuest={}", 
                record.getRoomId(), allRecords.size(), billDetail.getTotalAmount(), paymentMethod, pointsPerGuest);
    }
    
    /**
     * 获取会员等级排名（数字越大等级越高）
     */
    private int getMemberLevelRank(String memberLevel) {
        switch (memberLevel) {
            case "PLATINUM": return 4;
            case "GOLD": return 3;
            case "SILVER": return 2;
            case "BRONZE": return 1;
            default: return 0;
        }
    }
    
    /**
     * 根据会员等级获取折扣率
     */
    private BigDecimal getDiscountRate(String memberLevel) {
        switch (memberLevel) {
            case "PLATINUM": return new BigDecimal("0.85"); // 8.5折
            case "GOLD": return new BigDecimal("0.90");     // 9折
            case "SILVER": return new BigDecimal("0.95");   // 9.5折
            case "BRONZE": 
            default: return BigDecimal.ONE;                  // 不折扣
        }
    }
    
    /**
     * 根据房间ID获取账单详情
     */
    public BillDetailDTO getBillDetailByRoomId(Long roomId) {
        // 查询该房间第一个未退房的记录
        List<CheckInRecord> records = recordMapper.selectList(
            new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getRoomId, roomId)
                .isNull(CheckInRecord::getActualCheckout)
                .last("LIMIT 1")
        );
        
        if (records.isEmpty()) {
            throw new BusinessException("该房间暂无入住记录");
        }
        
        return getBillDetail(records.get(0).getRecordId());
    }
}

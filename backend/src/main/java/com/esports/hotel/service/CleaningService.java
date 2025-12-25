package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.CleaningRequest;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import com.esports.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 清扫服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleaningService {

    private final CleaningRecordMapper cleaningRecordMapper;
    private final SupplyUsageMapper supplyUsageMapper;
    private final RoomMapper roomMapper;
    private final StaffMapper staffMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 获取清扫记录列表
     */
    public Page<CleaningRecord> getCleaningRecords(Integer pageNum, Integer pageSize, 
                                                   String roomNumber, String cleaningType,
                                                   LocalDate startDate, LocalDate endDate) {
        Page<CleaningRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CleaningRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 按房间号查询
        if (roomNumber != null && !roomNumber.trim().isEmpty()) {
            LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(Room::getRoomNo, roomNumber.trim());
            Room room = roomMapper.selectOne(roomWrapper);
            if (room != null) {
                wrapper.eq(CleaningRecord::getRoomId, room.getRoomId());
            } else {
                // 房间不存在，返回空结果
                return page;
            }
        }
        
        // 按清扫类型查询
        if (cleaningType != null && !cleaningType.trim().isEmpty()) {
            wrapper.eq(CleaningRecord::getCleaningType, cleaningType);
        }
        
        // 按时间范围查询
        if (startDate != null) {
            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
            wrapper.ge(CleaningRecord::getCleaningTime, startDateTime);
        }
        if (endDate != null) {
            LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
            wrapper.le(CleaningRecord::getCleaningTime, endDateTime);
        }
        
        // 按清扫时间倒序
        wrapper.orderByDesc(CleaningRecord::getCleaningTime);
        
        Page<CleaningRecord> resultPage = cleaningRecordMapper.selectPage(page, wrapper);
        
        // 填充关联数据
        for (CleaningRecord record : resultPage.getRecords()) {
            // 填充房间信息
            Room room = roomMapper.selectById(record.getRoomId());
            if (room != null) {
                record.setRoomNumber(room.getRoomNo());
            }
            
            // 填充员工信息
            Staff staff = staffMapper.selectById(record.getStaffId());
            if (staff != null) {
                User user = userMapper.selectById(staff.getUserId());
                if (user != null) {
                    record.setStaffName(user.getUsername());
                }
            }
            
            // 填充物资消耗信息
            LambdaQueryWrapper<SupplyUsage> usageWrapper = new LambdaQueryWrapper<>();
            usageWrapper.eq(SupplyUsage::getCleaningId, record.getCleaningId());
            List<SupplyUsage> usages = supplyUsageMapper.selectList(usageWrapper);
            
            for (SupplyUsage usage : usages) {
                Product product = productMapper.selectById(usage.getProductId());
                if (product != null) {
                    usage.setProductName(product.getProductName());
                }
            }
            record.setSupplyUsages(usages);
        }
        
        return resultPage;
    }

    /**
     * 完成清扫并记录物资消耗
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeCleaning(CleaningRequest request, String token) {
        // 1. 验证员工身份
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long userId = jwtUtil.getUserIdFromToken(jwtToken);
        
        User user = userMapper.selectById(userId);
        if (user == null || !"STAFF".equals(user.getUserType())) {
            throw new BusinessException("只有员工可以执行清扫操作");
        }

        LambdaQueryWrapper<Staff> staffWrapper = new LambdaQueryWrapper<>();
        staffWrapper.eq(Staff::getUserId, userId);
        Staff staff = staffMapper.selectOne(staffWrapper);
        if (staff == null) {
            throw new BusinessException("员工信息不存在");
        }

        // 2. 查询房间
        LambdaQueryWrapper<Room> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Room::getRoomNo, request.getRoomNo());
        Room room = roomMapper.selectOne(roomWrapper);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        // 3. 创建清扫记录
        CleaningRecord cleaningRecord = new CleaningRecord();
        cleaningRecord.setRoomId(room.getRoomId());
        cleaningRecord.setStaffId(staff.getStaffId());
        cleaningRecord.setCleaningTime(LocalDateTime.now());
        cleaningRecord.setCleaningType(request.getCleaningType());
        cleaningRecord.setNotes(request.getNotes());
        cleaningRecordMapper.insert(cleaningRecord);

        log.info("创建清扫记录成功: cleaningId={}, roomNo={}, staffId={}, cleaningType={}", 
                cleaningRecord.getCleaningId(), request.getRoomNo(), staff.getStaffId(), request.getCleaningType());

        // 4. 记录物资消耗并更新库存
        if (request.getSupplies() != null && !request.getSupplies().isEmpty()) {
            for (CleaningRequest.SupplyItem item : request.getSupplies()) {
                // 4.1 查询商品
                Product product = productMapper.selectById(item.getProductId());
                if (product == null) {
                    log.warn("物资不存在: productId={}", item.getProductId());
                    continue;
                }

                // 4.2 检查库存
                if (product.getStockQuantity() < item.getQuantity()) {
                    log.warn("物资库存不足: productId={}, productName={}, stockQuantity={}, requestQuantity={}", 
                            product.getProductId(), product.getProductName(), 
                            product.getStockQuantity(), item.getQuantity());
                    throw new BusinessException(String.format("物资 %s 库存不足，当前库存：%d，需要：%d", 
                            product.getProductName(), product.getStockQuantity(), item.getQuantity()));
                }

                // 4.3 创建消耗记录
                SupplyUsage usage = new SupplyUsage();
                usage.setCleaningId(cleaningRecord.getCleaningId());
                usage.setProductId(item.getProductId());
                usage.setQuantity(item.getQuantity());
                usage.setUsageTime(LocalDateTime.now());
                supplyUsageMapper.insert(usage);

                // 4.4 更新商品库存
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                
                // 4.5 如果是可重复使用物品，更新使用次数
                if (product.getMaxUsageTimes() != null && product.getMaxUsageTimes() > 0) {
                    int newUsageTimes = product.getCurrentUsageTimes() + item.getQuantity();
                    // 如果达到最大使用次数，减少库存
                    if (newUsageTimes >= product.getMaxUsageTimes()) {
                        int itemsToReplace = newUsageTimes / product.getMaxUsageTimes();
                        product.setStockQuantity(product.getStockQuantity() - itemsToReplace);
                        product.setCurrentUsageTimes(newUsageTimes % product.getMaxUsageTimes());
                        log.info("可重复使用物品达到更换次数: productId={}, productName={}, 更换数量={}", 
                                product.getProductId(), product.getProductName(), itemsToReplace);
                    } else {
                        product.setCurrentUsageTimes(newUsageTimes);
                    }
                }
                
                productMapper.updateById(product);

                log.info("记录物资消耗: usageId={}, productId={}, productName={}, quantity={}, 剩余库存={}", 
                        usage.getUsageId(), product.getProductId(), product.getProductName(), 
                        item.getQuantity(), product.getStockQuantity());
            }
        }

        // 5. 更新房间状态为空闲
        if ("CHECKOUT".equals(request.getCleaningType())) {
            room.setStatus("VACANT");
            room.setCurrentOccupancy(0);
            roomMapper.updateById(room);
            log.info("清扫完成，房间状态更新为VACANT: roomNo={}", request.getRoomNo());
        }
    }
}

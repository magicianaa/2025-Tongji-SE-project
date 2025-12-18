package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.BusinessException;
import com.esports.hotel.dto.CreateOrderRequest;
import com.esports.hotel.dto.PosOrderVO;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * POS订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosOrderService {
    
    private final PosOrderMapper posOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final RoomMapper roomMapper;
    private final GuestMapper guestMapper;
    private final UserMapper userMapper;
    private final ProductService productService;
    
    /**
     * 创建订单（住客端）
     */
    @Transactional(rollbackFor = Exception.class)
    public PosOrderVO createOrder(Long recordId, CreateOrderRequest request) {
        // 1. 验证入住记录
        CheckInRecord checkIn = checkInRecordMapper.selectById(recordId);
        if (checkIn == null) {
            throw new BusinessException("入住记录不存在");
        }
        if (checkIn.getActualCheckout() != null) {
            throw new BusinessException("已退房，无法下单");
        }
        
        // 2. 创建订单
        PosOrder order = new PosOrder();
        order.setRecordId(recordId);
        order.setOrderNo(generateOrderNo());
        order.setOrderType(request.getOrderType());
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 3. 处理订单明细
        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderRequest.OrderItemDTO itemDTO : request.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: " + itemDTO.getProductId());
            }
            if (!product.getIsAvailable()) {
                throw new BusinessException("商品已下架: " + product.getProductName());
            }
            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new BusinessException("库存不足: " + product.getProductName());
            }
            
            // 计算小计
            BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            
            // 创建订单明细
            OrderItem item = new OrderItem();
            item.setProductId(product.getProductId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);
            items.add(item);
            
            // 扣减库存
            productService.updateStock(product.getProductId(), -itemDTO.getQuantity());
        }
        
        order.setTotalAmount(totalAmount);
        posOrderMapper.insert(order);
        
        // 4. 保存订单明细
        for (OrderItem item : items) {
            item.setOrderId(order.getOrderId());
            orderItemMapper.insert(item);
        }
        
        log.info("创建订单成功: orderId={}, orderNo={}, recordId={}, amount={}", 
                order.getOrderId(), order.getOrderNo(), recordId, totalAmount);
        
        return getOrderVO(order.getOrderId());
    }
    
    /**
     * 标记订单为已配送
     */
    @Transactional(rollbackFor = Exception.class)
    public PosOrderVO deliverOrder(Long orderId, Long operatorId) {
        PosOrder order = posOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }
        
        order.setStatus("DELIVERED");
        order.setOperatorId(operatorId);
        order.setDeliveryTime(LocalDateTime.now());
        posOrderMapper.updateById(order);
        
        log.info("订单已配送: orderId={}, orderNo={}, operatorId={}", 
                orderId, order.getOrderNo(), operatorId);
        
        return getOrderVO(orderId);
    }
    
    /**
     * 归还租赁设备
     */
    @Transactional(rollbackFor = Exception.class)
    public PosOrderVO returnRental(Long orderId) {
        PosOrder order = posOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"RENTAL".equals(order.getOrderType())) {
            throw new BusinessException("非租赁订单无需归还");
        }
        if ("RETURNED".equals(order.getStatus())) {
            throw new BusinessException("订单已归还");
        }
        
        order.setStatus("RETURNED");
        order.setReturnTime(LocalDateTime.now());
        posOrderMapper.updateById(order);
        
        // 恢复库存
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        for (OrderItem item : items) {
            productService.updateStock(item.getProductId(), item.getQuantity());
        }
        
        log.info("租赁设备已归还: orderId={}, orderNo={}", orderId, order.getOrderNo());
        
        return getOrderVO(orderId);
    }
    
    /**
     * 获取住客的订单列表
     */
    public List<PosOrderVO> getOrdersByRecordId(Long recordId) {
        LambdaQueryWrapper<PosOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PosOrder::getRecordId, recordId)
               .orderByDesc(PosOrder::getCreateTime);
        
        List<PosOrder> orders = posOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(order -> getOrderVO(order.getOrderId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取待配送订单（前台端）
     */
    public List<PosOrderVO> getPendingOrders() {
        LambdaQueryWrapper<PosOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PosOrder::getStatus, "PENDING")
               .orderByAsc(PosOrder::getCreateTime);
        
        List<PosOrder> orders = posOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(order -> getOrderVO(order.getOrderId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有订单（前台端）
     */
    public List<PosOrderVO> getAllOrders(String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PosOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PosOrder::getStatus, status);
        }
        
        if (startTime != null) {
            wrapper.ge(PosOrder::getCreateTime, startTime);
        }
        
        if (endTime != null) {
            wrapper.le(PosOrder::getCreateTime, endTime);
        }
        
        wrapper.orderByDesc(PosOrder::getCreateTime);
        
        List<PosOrder> orders = posOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(order -> getOrderVO(order.getOrderId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 构建订单VO
     */
    private PosOrderVO getOrderVO(Long orderId) {
        PosOrder order = posOrderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        
        PosOrderVO vo = new PosOrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType(order.getOrderType());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setReturnTime(order.getReturnTime());
        
        // 获取房间号
        CheckInRecord checkIn = checkInRecordMapper.selectById(order.getRecordId());
        if (checkIn != null) {
            Room room = roomMapper.selectById(checkIn.getRoomId());
            if (room != null) {
                vo.setRoomNo(room.getRoomNo());
            }
            
            // 获取住客姓名
            Guest guest = guestMapper.selectById(checkIn.getGuestId());
            if (guest != null) {
                vo.setGuestName(guest.getRealName());
            }
        }
        
        // 获取操作员用户名
        if (order.getOperatorId() != null) {
            User operator = userMapper.selectById(order.getOperatorId());
            if (operator != null) {
                vo.setOperatorName(operator.getUsername());
            }
        }
        
        // 获取订单明细
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        
        List<PosOrderVO.OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderItem item : items) {
            PosOrderVO.OrderItemVO itemVO = new PosOrderVO.OrderItemVO();
            itemVO.setItemId(item.getItemId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setSubtotal(item.getSubtotal());
            
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                itemVO.setProductName(product.getProductName());
                itemVO.setProductType(product.getProductType());
            }
            
            itemVOs.add(itemVO);
        }
        
        vo.setItems(itemVOs);
        
        return vo;
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int) (Math.random() * 1000);
        return "POS" + timestamp + String.format("%03d", random);
    }
}

package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import com.esports.hotel.dto.CreateOrderRequest;
import com.esports.hotel.dto.PosOrderVO;
import com.esports.hotel.service.PosOrderService;
import com.esports.hotel.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * POS订单Controller
 */
@Slf4j
@RestController
@RequestMapping("/pos-orders")
@RequiredArgsConstructor
public class PosOrderController {
    
    private final PosOrderService posOrderService;
    private final JwtUtil jwtUtil;
    
    /**
     * 创建订单（住客端）
     */
    @PostMapping
    public Result<PosOrderVO> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        Long recordId = request.getRecordId();
        if (recordId == null) {
            return Result.fail("缺少入住记录ID");
        }
        
        PosOrderVO order = posOrderService.createOrder(recordId, request);
        return Result.success(order);
    }
    
    /**
     * 标记订单为已配送
     */
    @PutMapping("/{orderId}/deliver")
    public Result<PosOrderVO> deliverOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String authHeader) {
        
        // 从token获取操作员ID
        String token = authHeader.replace("Bearer ", "");
        Long operatorId = jwtUtil.getUserIdFromToken(token);
        
        PosOrderVO order = posOrderService.deliverOrder(orderId, operatorId);
        return Result.success(order);
    }
    
    /**
     * 归还租赁设备
     */
    @PutMapping("/{orderId}/return")
    public Result<PosOrderVO> returnRental(@PathVariable Long orderId) {
        PosOrderVO order = posOrderService.returnRental(orderId);
        return Result.success(order);
    }
    
    /**
     * 获取住客的订单列表
     */
    @GetMapping("/my-orders")
    public Result<List<PosOrderVO>> getMyOrders(@RequestParam Long recordId) {
        List<PosOrderVO> orders = posOrderService.getOrdersByRecordId(recordId);
        return Result.success(orders);
    }
    
    /**
     * 获取待配送订单（前台端）
     */
    @GetMapping("/pending")
    public Result<List<PosOrderVO>> getPendingOrders() {
        List<PosOrderVO> orders = posOrderService.getPendingOrders();
        return Result.success(orders);
    }
    
    /**
     * 获取所有订单（前台端）
     */
    @GetMapping
    public Result<List<PosOrderVO>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        List<PosOrderVO> orders = posOrderService.getAllOrders(status, startTime, endTime);
        return Result.success(orders);
    }
}

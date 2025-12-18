package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import com.esports.hotel.dto.BillDetailDTO;
import com.esports.hotel.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 账单管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    
    /**
     * 获取账单详情（根据入住记录ID）
     */
    @GetMapping("/detail/{recordId}")
    public Result<BillDetailDTO> getBillDetail(@PathVariable Long recordId) {
        BillDetailDTO billDetail = billingService.getBillDetail(recordId);
        return Result.success(billDetail);
    }
    
    /**
     * 获取账单详情（根据房间ID）
     */
    @GetMapping("/detail/room/{roomId}")
    public Result<BillDetailDTO> getBillDetailByRoomId(@PathVariable Long roomId) {
        BillDetailDTO billDetail = billingService.getBillDetailByRoomId(roomId);
        return Result.success(billDetail);
    }
    
    /**
     * 账单清付
     */
    @PostMapping("/settle/{recordId}")
    public Result<Void> settleBill(
            @PathVariable Long recordId,
            @RequestParam String paymentMethod) {
        billingService.settleBill(recordId, paymentMethod);
        return Result.success();
    }
}

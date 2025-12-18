package com.esports.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单详情DTO
 */
@Data
public class BillDetailDTO {
    
    /**
     * 入住记录ID
     */
    private Long recordId;
    
    /**
     * 房间号
     */
    private String roomNo;
    
    /**
     * 房型
     */
    private String roomType;
    
    /**
     * 入住时间
     */
    private LocalDateTime checkInTime;
    
    /**
     * 当前时间/结算时间
     */
    private LocalDateTime currentTime;
    
    /**
     * 入住天数
     */
    private Long stayDays;
    
    /**
     * 每天价格
     */
    private BigDecimal pricePerDay;
    
    /**
     * 房费小计
     */
    private BigDecimal roomFee;
    
    /**
     * POS订单列表
     */
    private List<PosOrderSummary> posOrders;
    
    /**
     * POS消费小计
     */
    private BigDecimal posTotal;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 待支付金额
     */
    private BigDecimal unpaidAmount;
    
    /**
     * POS订单摘要
     */
    @Data
    public static class PosOrderSummary {
        private Long orderId;
        private String orderNo;
        private String orderType;
        private LocalDateTime createTime;
        private BigDecimal totalAmount;
        private String status;
        private String guestName;
        private String phone;
    }
}

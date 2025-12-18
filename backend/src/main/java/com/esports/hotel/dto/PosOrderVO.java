package com.esports.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * POS订单视图对象
 */
@Data
public class PosOrderVO {
    private Long orderId;
    private Long recordId;
    private String orderNo;
    private String orderType;
    private LocalDateTime createTime;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime deliveryTime;
    private LocalDateTime returnTime;
    
    // 关联信息
    private String roomNo;
    private String guestName;
    private String operatorName;
    
    // 订单明细
    private List<OrderItemVO> items;
    
    @Data
    public static class OrderItemVO {
        private Long itemId;
        private Long productId;
        private String productName;
        private String productType;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private LocalDateTime rentalStart;
        private LocalDateTime rentalEnd;
        private String damageStatus;
    }
}

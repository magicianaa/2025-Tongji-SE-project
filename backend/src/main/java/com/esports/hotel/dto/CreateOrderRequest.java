package com.esports.hotel.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建订单请求
 */
@Data
public class CreateOrderRequest {
        private Long recordId;
        private String orderType;  // PURCHASE, RENTAL
    
    private List<OrderItemDTO> items;
    
    @Data
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
    }
}

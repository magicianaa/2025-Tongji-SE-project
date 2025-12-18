package com.esports.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品DTO
 */
@Data
public class ProductDTO {
    private Long productId;
    private String productName;
    private String productType;
    private String category;
    private BigDecimal price;
    private String rentalUnit;
    private Integer stockQuantity;
    private Integer stockThreshold;
    private String imageUrl;
    private String description;
    private Boolean isAvailable;
    
    // 库存预警标识
    private Boolean isLowStock;
}

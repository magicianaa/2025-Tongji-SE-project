package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@TableName("tb_product")
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Long productId;
    
    private String productName;
    
    private String productType;  // SNACK, BEVERAGE, PERIPHERAL
    
    private String category;
    
    private BigDecimal price;
    
    private String rentalUnit;  // NONE, HOURLY, DAILY
    
    private Integer stockQuantity;
    
    private Integer stockThreshold;
    
    private String imageUrl;
    
    private String description;
    
    private Boolean isAvailable;
}

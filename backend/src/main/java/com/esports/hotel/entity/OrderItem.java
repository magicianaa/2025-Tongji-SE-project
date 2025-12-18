package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体
 */
@Data
@TableName("tb_order_item")
public class OrderItem {
    
    @TableId(type = IdType.AUTO)
    private Long itemId;
    
    private Long orderId;
    
    private Long productId;
    
    private Integer quantity;
    
    private BigDecimal unitPrice;
    
    private BigDecimal subtotal;
    
    private LocalDateTime rentalStart;
    
    private LocalDateTime rentalEnd;
    
    private String damageStatus;  // NORMAL, DAMAGED
}

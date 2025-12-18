package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POS订单实体
 */
@Data
@TableName("tb_pos_order")
public class PosOrder {
    
    @TableId(type = IdType.AUTO)
    private Long orderId;
    
    private Long recordId;  // 关联入住记录
    
    private String orderNo;
    
    private String orderType;  // PURCHASE, RENTAL
    
    private LocalDateTime createTime;
    
    private String status;  // PENDING, DELIVERED, RETURNED, CANCELLED
    
    private BigDecimal totalAmount;
    
    private LocalDateTime deliveryTime;
    
    private LocalDateTime returnTime;
    
    private Long operatorId;
}

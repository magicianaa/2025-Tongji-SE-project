package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物资消耗记录实体
 */
@Data
@TableName("tb_supply_usage")
public class SupplyUsage implements Serializable {

    @TableId(value = "usage_id", type = IdType.AUTO)
    private Long usageId;

    /**
     * 关联清扫记录ID
     */
    private Long cleaningId;

    /**
     * 物资ID
     */
    private Long productId;

    /**
     * 消耗数量
     */
    private Integer quantity;

    /**
     * 消耗时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime usageTime;
    
    /**
     * 物资名称（临时字段，不存储在数据库）
     */
    @TableField(exist = false)
    private String productName;
}

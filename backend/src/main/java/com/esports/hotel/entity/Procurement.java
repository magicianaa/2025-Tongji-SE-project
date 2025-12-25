package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 进货记录实体
 */
@Data
@TableName("tb_procurement")
public class Procurement implements Serializable {

    @TableId(value = "procurement_id", type = IdType.AUTO)
    private Long procurementId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 进货数量
     */
    private Integer quantity;

    /**
     * 进货单价
     */
    private BigDecimal unitCost;

    /**
     * 总成本
     */
    private BigDecimal totalCost;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 进货时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime procurementTime;

    /**
     * 操作员工ID
     */
    private Long operatorId;

    /**
     * 备注
     */
    private String notes;
}

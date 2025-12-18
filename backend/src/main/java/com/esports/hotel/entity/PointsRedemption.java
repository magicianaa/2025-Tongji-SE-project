package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分兑换记录表实体
 */
@Data
@TableName("tb_points_redemption")
public class PointsRedemption implements Serializable {

    @TableId(value = "redemption_id", type = IdType.AUTO)
    private Long redemptionId;

    /**
     * 住客ID
     */
    private Long guestId;

    /**
     * 积分商品ID
     */
    private Long pointsProductId;

    /**
     * 消耗积分
     */
    private Integer pointsCost;

    /**
     * 兑换时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime redemptionTime;

    /**
     * 状态：PENDING, FULFILLED, CANCELLED
     */
    private String status;

    /**
     * 履约备注
     */
    private String fulfillmentNotes;
}

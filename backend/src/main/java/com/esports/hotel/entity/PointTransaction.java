package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分流水表实体
 */
@Data
@TableName("tb_point_transaction")
public class PointTransaction implements Serializable {

    @TableId(value = "transaction_id", type = IdType.AUTO)
    private Long transactionId;

    /**
     * 住客ID
     */
    private Long guestId;

    /**
     * 积分变动数（正为获得，负为消耗）
     */
    private Integer amount;

    /**
     * 交易类型：TASK_REWARD, REDEMPTION, REFUND, ADMIN_ADJUST, CHECKIN_REWARD
     */
    private String transactionType;

    /**
     * 关联业务ID（如任务ID、兑换订单ID）
     */
    private Long relatedId;

    /**
     * 描述
     */
    private String description;

    /**
     * 变动后余额快照
     */
    private Integer balanceAfter;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

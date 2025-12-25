package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住记录表实体（二次鉴权核心表）
 */
@Data
@TableName("tb_checkin_record")
public class CheckInRecord implements Serializable {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /**
     * 关联预订ID（Walk-in时为NULL）
     */
    private Long bookingId;

    private Long roomId;

    /**
     * 主住客ID
     */
    private Long guestId;

    /**
     * 实际入住时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime actualCheckin;

    /**
     * 实际退房时间
     */
    private LocalDateTime actualCheckout;

    /**
     * 预期退房时间
     */
    private LocalDateTime expectedCheckout;

    /**
     * 是否拥有客房权限（二次鉴权状态）
     * TRUE: 可以访问客房控制接口
     * FALSE: 权限已回收
     */
    private Boolean isGamingAuthActive;

    /**
     * 房费小计
     */
    private BigDecimal roomFee;

    /**
     * 赔偿金
     */
    private BigDecimal damageCompensation;

    /**
     * 使用积分抵扣金额
     */
    private Integer pointsDeduction;

    /**
     * 最终应付金额
     */
    private BigDecimal finalAmount;

    /**
     * 支付状态：UNPAID, PAID
     */
    private String paymentStatus;

    /**
     * 支付方式：CASH, WECHAT, ALIPAY, CARD
     */
    private String paymentMethod;

    /**
     * 备注（如换房记录）
     */
    private String notes;

    // ========== 以下字段不存储在数据库，仅用于返回给前端 ==========

    /**
     * 房间号（临时字段）
     */
    @TableField(exist = false)
    private String roomNo;

    /**
     * 房间类型（临时字段）
     */
    @TableField(exist = false)
    private String roomType;

    /**
     * 是否已评价（临时字段）
     */
    @TableField(exist = false)
    private Boolean hasReviewed;

    /**
     * 评价信息（临时字段）
     */
    @TableField(exist = false)
    private Review review;
}

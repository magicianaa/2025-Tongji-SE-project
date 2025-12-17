package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预订记录实体
 */
@Data
@TableName("tb_booking")
public class Booking implements Serializable {

    @TableId(value = "booking_id", type = IdType.AUTO)
    private Long bookingId;

    /**
     * 住客ID
     */
    private Long guestId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 预订时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime bookingTime;

    /**
     * 计划入住时间
     */
    private LocalDateTime plannedCheckin;

    /**
     * 计划退房时间
     */
    private LocalDateTime plannedCheckout;

    /**
     * 预订状态：PENDING, CONFIRMED, CHECKED_IN, CANCELLED
     */
    private String status;

    /**
     * 预付押金
     */
    private BigDecimal depositAmount;

    /**
     * 折扣率（会员享受）
     */
    private BigDecimal discountRate;

    /**
     * 特殊要求
     */
    private String specialRequests;

    /**
     * 主住客姓名（用于入住验证）
     */
    private String mainGuestName;

    /**
     * 联系电话（用于入住验证）
     */
    private String contactPhone;
}

package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报警记录表实体
 */
@Data
@TableName("tb_alert_log")
public class AlertLog implements Serializable {

    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long alertId;

    private Long roomId;

    /**
     * 报警类型：OVERHEAT, NETWORK_FAIL, DEVICE_OFFLINE
     */
    private String alertType;

    /**
     * 报警级别：WARNING, CRITICAL
     */
    private String alertLevel;

    /**
     * 触发值（如：98°C）
     */
    private String triggerValue;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime alertTime;

    /**
     * 是否已处理
     */
    private Boolean isHandled;

    private Long handlerId;

    private LocalDateTime handleTime;
}

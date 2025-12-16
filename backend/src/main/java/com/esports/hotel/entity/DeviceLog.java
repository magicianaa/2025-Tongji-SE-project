package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 硬件日志表实体（归档数据，用于趋势分析）
 */
@Data
@TableName("tb_device_log")
public class DeviceLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long roomId;

    private Float cpuTemp;

    private Float gpuTemp;

    private Integer networkLatency;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime logTime;
}

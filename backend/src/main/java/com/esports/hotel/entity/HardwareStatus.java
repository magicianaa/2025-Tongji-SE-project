package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 硬件状态表实体（实时数据）
 */
@Data
@TableName("tb_hardware_status")
public class HardwareStatus implements Serializable {

    @TableId(value = "status_id", type = IdType.AUTO)
    private Long statusId;

    private Long roomId;

    /**
     * CPU温度（°C）
     */
    private Float cpuTemp;

    /**
     * GPU温度（°C）
     */
    private Float gpuTemp;

    /**
     * 网络延迟（ms）
     */
    private Integer networkLatency;

    /**
     * 外设连接状态（JSON格式）
     */
    private String peripheralStatus;

    /**
     * 健康等级：GREEN, YELLOW, RED
     */
    private String healthLevel;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdate;
}

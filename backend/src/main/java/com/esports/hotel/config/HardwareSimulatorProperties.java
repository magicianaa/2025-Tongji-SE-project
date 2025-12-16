package com.esports.hotel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 硬件模拟器配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "hardware.simulator")
public class HardwareSimulatorProperties {

    /**
     * 是否启用模拟器
     */
    private Boolean enabled = true;

    /**
     * 数据生成间隔（秒）
     */
    private Integer interval = 5;

    /**
     * 温度范围（°C）
     */
    private Float tempMin = 40.0f;
    private Float tempMax = 95.0f;

    /**
     * 温度报警阈值
     */
    private Float tempAlert = 95.0f;

    /**
     * 网络延迟范围（ms）
     */
    private Integer latencyMin = 10;
    private Integer latencyMax = 100;

    /**
     * 故障模拟概率（0-1）
     */
    private Float failureRate = 0.05f;
}

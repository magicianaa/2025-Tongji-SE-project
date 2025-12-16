package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 硬件遥测数据 DTO（WebSocket推送使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "硬件遥测数据")
public class HardwareTelemetryDTO {

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "CPU温度（°C）")
    private Float cpuTemp;

    @Schema(description = "GPU温度（°C）")
    private Float gpuTemp;

    @Schema(description = "网络延迟（ms）")
    private Integer networkLatency;

    @Schema(description = "外设连接状态")
    private PeripheralStatus peripheralStatus;

    @Schema(description = "健康等级（GREEN/YELLOW/RED）")
    private String healthLevel;

    @Schema(description = "数据时间戳")
    private LocalDateTime timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeripheralStatus {
        @Schema(description = "键盘是否在线")
        private Boolean keyboard;

        @Schema(description = "鼠标是否在线")
        private Boolean mouse;

        @Schema(description = "耳机是否在线")
        private Boolean headset;
    }
}

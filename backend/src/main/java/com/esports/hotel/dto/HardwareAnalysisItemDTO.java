package com.esports.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 硬件损耗统计项DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HardwareAnalysisItemDTO {

    /**
     * 设备类型（如：鼠标、键盘、显示器等）
     */
    private String deviceType;

    /**
     * 品牌/型号
     */
    private String brandModel;

    /**
     * 故障次数（近30天）
     */
    private Integer failureCount;

    /**
     * 涉及房间数
     */
    private Integer affectedRoomCount;

    /**
     * 故障率（故障次数/总设备数）
     */
    private Double failureRate;

    /**
     * 平均修复时间（小时）
     */
    private Double avgRepairTime;

    /**
     * 建议采购数量
     */
    private Integer recommendedPurchaseQty;

    /**
     * 建议原因
     */
    private String recommendationReason;
}

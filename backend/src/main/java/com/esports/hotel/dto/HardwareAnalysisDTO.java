package com.esports.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 硬件损耗分析响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HardwareAnalysisDTO {

    /**
     * 分析时间范围（天数）
     */
    private Integer analysisDays;

    /**
     * 总维修工单数
     */
    private Integer totalMaintenanceTickets;

    /**
     * 各设备类型损耗统计
     */
    private List<HardwareAnalysisItemDTO> analysisItems;

    /**
     * 高频故障设备TOP3
     */
    private List<HardwareAnalysisItemDTO> topFailureDevices;

    /**
     * 建议采购清单
     */
    private List<HardwareAnalysisItemDTO> purchaseRecommendations;
}

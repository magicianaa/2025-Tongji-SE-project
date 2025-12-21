package com.esports.hotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 财务报表DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportDTO {

    /**
     * 报表日期（日报）或月份（月报）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    /**
     * 报表类型：DAILY(日报) / MONTHLY(月报)
     */
    private String reportType;

    /**
     * 客房收入
     */
    private BigDecimal roomRevenue;

    /**
     * POS销售收入
     */
    private BigDecimal posRevenue;

    /**
     * 积分兑换收入（积分折算）
     */
    private BigDecimal pointsRevenue;

    /**
     * 总收入
     */
    private BigDecimal totalRevenue;

    /**
     * 入住订单数
     */
    private Integer checkInCount;

    /**
     * POS订单数
     */
    private Integer posOrderCount;

    /**
     * 平均客单价（房费）
     */
    private BigDecimal avgRoomPrice;

    /**
     * 平均POS消费
     */
    private BigDecimal avgPosConsumption;

    /**
     * 入住率（百分比）
     */
    private BigDecimal occupancyRate;

    /**
     * RevPAR（平均客房收益）
     */
    private BigDecimal revPAR;
}

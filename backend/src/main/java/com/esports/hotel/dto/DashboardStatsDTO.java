package com.esports.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 运营看板统计数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * 总房间数
     */
    private Integer totalRooms;

    /**
     * 当前入住房间数
     */
    private Integer occupiedRooms;

    /**
     * 入住率（百分比）
     */
    private BigDecimal occupancyRate;

    /**
     * RevPAR（Revenue Per Available Room - 平均客房收益）
     * 计算公式：总客房收入 / 可用房间数
     */
    private BigDecimal revPAR;

    /**
     * 今日入住数
     */
    private Integer todayCheckIns;

    /**
     * 今日退房数
     */
    private Integer todayCheckOuts;

    /**
     * 待处理报警数
     */
    private Integer pendingAlerts;

    /**
     * 待处理维修工单数
     */
    private Integer pendingMaintenanceTickets;

    /**
     * 本月总收入
     */
    private BigDecimal monthlyRevenue;

    /**
     * 本月完成订单数
     */
    private Integer monthlyOrders;

    /**
     * 活跃会员数
     */
    private Integer activeMembers;
}

package com.esports.hotel.service;

import com.esports.hotel.dto.DashboardStatsDTO;
import com.esports.hotel.dto.FinancialReportDTO;
import com.esports.hotel.dto.HardwareAnalysisDTO;

import java.time.LocalDate;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 获取运营看板统计数据
     *
     * @return 运营看板数据
     */
    DashboardStatsDTO getDashboardStats();

    /**
     * 生成财务日报
     *
     * @param date 报表日期
     * @return 财务日报数据
     */
    FinancialReportDTO getDailyReport(LocalDate date);

    /**
     * 生成财务月报
     *
     * @param year  年份
     * @param month 月份
     * @return 财务月报数据
     */
    FinancialReportDTO getMonthlyReport(Integer year, Integer month);

    /**
     * 获取硬件损耗分析与采购建议
     *
     * @param days 分析天数（默认30天）
     * @return 硬件损耗分析数据
     */
    HardwareAnalysisDTO getHardwareAnalysis(Integer days);

    /**
     * 导出财务报表为Excel
     *
     * @param reportType 报表类型：DAILY / MONTHLY
     * @param date       报表日期
     * @return Excel文件字节数组
     */
    byte[] exportFinancialReportExcel(String reportType, LocalDate date);
}

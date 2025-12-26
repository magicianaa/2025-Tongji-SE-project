package com.esports.hotel.service;

import com.esports.hotel.dto.FinancialReportDTO;

/**
 * 千问AI服务接口
 */
public interface QianWenService {

    /**
     * 生成AI财报分析
     *
     * @param currentReport 当前财报数据
     * @param reportType 报表类型：DAILY / MONTHLY
     * @return AI生成的财报分析和建议
     */
    String generateFinancialAnalysis(FinancialReportDTO currentReport, String reportType);
}

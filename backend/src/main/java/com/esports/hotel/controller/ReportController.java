package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import com.esports.hotel.dto.DashboardStatsDTO;
import com.esports.hotel.dto.FinancialReportDTO;
import com.esports.hotel.dto.HardwareAnalysisDTO;
import com.esports.hotel.service.QianWenService;
import com.esports.hotel.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 报表与决策支持控制器
 */
@Tag(name = "报表与决策支持", description = "运营看板、财务报表、硬件损耗分析")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final QianWenService qianWenService;

    // ==================== FR-RPT-01 运营看板 ====================

    /**
     * 获取运营看板数据
     */
    @Operation(summary = "获取运营看板", description = "实时展示入住率、RevPAR、待处理报警数等关键指标")
    @GetMapping("/dashboard")
    public Result<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = reportService.getDashboardStats();
        return Result.success(stats);
    }

    // ==================== FR-RPT-02 财务报表 ====================

    /**
     * 获取财务日报
     */
    @Operation(summary = "获取财务日报", description = "生成指定日期的财务日报")
    @GetMapping("/financial/daily")
    public Result<FinancialReportDTO> getDailyReport(
            @Parameter(description = "报表日期，格式：yyyy-MM-dd，默认为今天")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        FinancialReportDTO report = reportService.getDailyReport(date);
        return Result.success(report);
    }

    /**
     * 获取财务月报
     */
    @Operation(summary = "获取财务月报", description = "生成指定年月的财务月报")
    @GetMapping("/financial/monthly")
    public Result<FinancialReportDTO> getMonthlyReport(
            @Parameter(description = "年份，默认为当前年份")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "月份（1-12），默认为当前月份")
            @RequestParam(required = false) Integer month) {
        
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        
        FinancialReportDTO report = reportService.getMonthlyReport(year, month);
        return Result.success(report);
    }

    /**
     * 导出财务报表为Excel/CSV
     */
    @Operation(summary = "导出财务报表", description = "导出财务报表为CSV文件（支持Excel打开）")
    @GetMapping("/financial/export")
    public ResponseEntity<byte[]> exportFinancialReport(
            @Parameter(description = "报表类型：DAILY(日报) / MONTHLY(月报)")
            @RequestParam String reportType,
            @Parameter(description = "报表日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        byte[] fileContent = reportService.exportFinancialReportExcel(reportType, date);
        
        String filename = "financial_report_" + reportType.toLowerCase() + "_" + date + ".csv";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(fileContent);
    }

    // ==================== FR-RPT-03 硬件损耗分析 ====================

    /**
     * 获取硬件损耗分析与采购建议
     */
    @Operation(summary = "硬件损耗分析", description = "分析近N天维修工单，识别高频故障外设，生成采购建议")
    @GetMapping("/hardware/analysis")
    public Result<HardwareAnalysisDTO> getHardwareAnalysis(
            @Parameter(description = "分析天数，默认30天")
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        
        HardwareAnalysisDTO analysis = reportService.getHardwareAnalysis(days);
        return Result.success(analysis, "硬件损耗分析完成");
    }

    /**
     * 获取采购预测建议（高频故障设备）
     */
    @Operation(summary = "采购预测建议", description = "基于维修日志生成建议采购清单")
    @GetMapping("/hardware/purchase-recommendations")
    public Result<?> getPurchaseRecommendations(
            @Parameter(description = "分析天数，默认30天")
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        
        HardwareAnalysisDTO analysis = reportService.getHardwareAnalysis(days);
        
        if (analysis.getPurchaseRecommendations().isEmpty()) {
            return Result.success(analysis.getPurchaseRecommendations(), 
                    "当前无需采购，所有设备运行状况良好");
        }
        
        return Result.success(analysis.getPurchaseRecommendations(), 
                "已生成" + analysis.getPurchaseRecommendations().size() + "项采购建议");
    }

    // ==================== AI财报分析 ====================

    /**
     * 生成AI财报分析
     */
    @Operation(summary = "生成AI财报分析", description = "使用千问AI生成专业的财报分析和经营建议")
    @GetMapping("/financial/ai-analysis")
    public Result<String> generateAIAnalysis(
            @Parameter(description = "报表类型：DAILY(日报) / MONTHLY(月报)")
            @RequestParam String reportType,
            @Parameter(description = "报表日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        // 先获取财报数据
        FinancialReportDTO report;
        if ("DAILY".equalsIgnoreCase(reportType)) {
            report = reportService.getDailyReport(date);
        } else if ("MONTHLY".equalsIgnoreCase(reportType)) {
            report = reportService.getMonthlyReport(date.getYear(), date.getMonthValue());
        } else {
            return Result.fail("报表类型错误，仅支持 DAILY 或 MONTHLY");
        }
        
        // 调用AI生成分析
        String analysis = qianWenService.generateFinancialAnalysis(report, reportType);
        
        return Result.success(analysis, "AI财报分析生成成功");
    }
}

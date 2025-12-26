package com.esports.hotel.service.impl;

import com.esports.hotel.dto.FinancialReportDTO;
import com.esports.hotel.service.QianWenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 千问AI服务实现类
 */
@Slf4j
@Service
public class QianWenServiceImpl implements QianWenService {

    private static final String API_KEY = "sk-bd89293ecbbf4cb7a291bfb5d83496e0";
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String MODEL = "qwen-plus";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public QianWenServiceImpl() {
        // 配置HTTP客户端，增加超时时间
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(50))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(50, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(50, TimeUnit.SECONDS))
                );

        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + API_KEY)
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateFinancialAnalysis(FinancialReportDTO currentReport, String reportType) {
        try {
            log.info("开始生成AI财报分析，报表类型: {}, 日期: {}", reportType, currentReport.getReportDate());
            
            String prompt = buildPrompt(currentReport, reportType);
            log.debug("提示词长度: {} 字符", prompt.length());
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            
            long startTime = System.currentTimeMillis();
            
            log.info("正在调用千问API...");
            
            // 调用API
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("API调用失败: {}", error.getMessage()))
                    .onErrorResume(error -> {
                        log.error("WebClient错误", error);
                        return reactor.core.publisher.Mono.error(
                            new RuntimeException("调用千问API失败: " + error.getMessage(), error)
                        );
                    })
                    .block(Duration.ofSeconds(50));
            
            long endTime = System.currentTimeMillis();
            log.info("千问API调用成功，耗时: {}ms", (endTime - startTime));
            
            if (response == null || response.isEmpty()) {
                log.error("API返回空响应");
                return "AI财报分析生成失败：API返回空响应";
            }
            
            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // 检查是否有错误
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                log.error("千问API返回错误: {}", errorMsg);
                return "AI财报分析生成失败：" + errorMsg;
            }
            
            String analysis = jsonNode.path("choices").get(0)
                    .path("message").path("content").asText();
            
            if (analysis == null || analysis.isEmpty()) {
                log.error("从响应中解析内容失败");
                return "AI财报分析生成失败：无法解析API响应内容";
            }
            
            log.info("AI财报分析生成成功，报表类型: {}, 日期: {}, 内容长度: {} 字符", 
                    reportType, currentReport.getReportDate(), analysis.length());
            
            return analysis;
        } catch (Exception e) {
            log.error("AI财报分析生成失败", e);
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Connection prematurely closed")) {
                return "AI财报分析生成失败：网络连接被提前关闭。\n\n可能原因：\n1. 千问API服务暂时不可用\n2. 网络连接不稳定\n3. API密钥可能无效\n\n建议稍后重试。";
            } else if (errorMsg.contains("timeout")) {
                return "AI财报分析生成失败：请求超时。\n\n千问AI服务响应时间较长，请稍后重试。";
            }
            return "AI财报分析生成失败: " + errorMsg + "\n\n请检查网络连接和API配置，然后重试。";
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(FinancialReportDTO report, String reportType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        String dateStr = report.getReportDate().format(formatter);
        String reportTypeName = "DAILY".equals(reportType) ? "日报" : "月报";

        return String.format("""
                作为电竞酒店财务分析专家，分析以下%s数据（%s）：
                
                **收入**：房费¥%s，POS¥%s，积分¥%s，总计¥%s
                **支出**：进货¥%s，维修¥%s，总计¥%s
                **利润**：¥%s
                **运营**：入住%d单，POS%d单，客单¥%s，入住率%s%%，RevPAR¥%s
                
                请以Markdown格式提供：
                
                ## 1. 财务总结
                简要评价本期表现（2-3句）
                
                ## 2. 收入分析
                - 收入结构占比
                - 优势与不足
                
                ## 3. 成本控制
                - 成本合理性
                - 优化建议
                
                ## 4. 运营效率
                - 入住率与RevPAR评价
                - 客单价分析
                
                ## 5. 风险预警
                列出2-3个主要风险点
                
                ## 6. 改进建议
                - 提升收入：3条具体措施
                - 控制成本：2条建议
                - 运营优化：2条方案
                
                ## 7. 未来展望
                基于数据的趋势预测和战略建议（2-3句）
                
                要求：专业、简洁、可操作，每部分控制在150字以内。
                """,
                reportTypeName, dateStr,
                report.getRoomRevenue(), report.getPosRevenue(), report.getPointsRevenue(), report.getTotalRevenue(),
                report.getProcurementCost(), report.getMaintenanceCost(), report.getTotalExpense(),
                report.getNetProfit(),
                report.getCheckInCount(), report.getPosOrderCount(),
                report.getAvgRoomPrice(), report.getOccupancyRate(), report.getRevPAR()
        );
    }
}

package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 测试控制器 - 用于测试千问API
 */
@Slf4j
@Tag(name = "测试接口", description = "用于测试功能")
@RestController
@RequestMapping("/test")
public class TestController {

    private static final String API_KEY = "sk-bd89293ecbbf4cb7a291bfb5d83496e0";
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    @Operation(summary = "测试千问API", description = "简单测试千问API是否可用")
    @GetMapping("/qianwen")
    public Result<String> testQianWen() {
        try {
            log.info("开始测试千问API...");
            
            // 配置HTTP客户端
            HttpClient httpClient = HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .responseTimeout(Duration.ofSeconds(50))
                    .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(50, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(50, TimeUnit.SECONDS))
                    );

            WebClient webClient = WebClient.builder()
                    .baseUrl(BASE_URL)
                    .defaultHeader("Authorization", "Bearer " + API_KEY)
                    .defaultHeader("Content-Type", "application/json")
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
            
            // 构建简单的请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-plus");
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", "你好，请用一句话介绍你自己。")
            ));
            
            long startTime = System.currentTimeMillis();
            log.info("发送请求到千问API...");
            
            // 调用API
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(error -> log.error("API调用失败: {}", error.getMessage()))
                    .block(Duration.ofSeconds(50));
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("千问API调用成功！耗时: {}ms", duration);
            log.info("响应内容: {}", response);
            
            return Result.success("测试成功！耗时: " + duration + "ms\n\n响应:\n" + response);
            
        } catch (Exception e) {
            log.error("测试失败", e);
            return Result.fail("测试失败: " + e.getMessage());
        }
    }
}

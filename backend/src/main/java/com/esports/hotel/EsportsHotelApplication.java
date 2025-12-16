package com.esports.hotel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智慧电竞酒店管理系统 - 主启动类
 * 
 * @author Smart Esports Hotel Dev Team
 * @since 2025-12-15
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务（硬件模拟器）
@EnableAsync       // 启用异步任务
@MapperScan("com.esports.hotel.mapper")
public class EsportsHotelApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsportsHotelApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════════╗
            ║                                                           ║
            ║   Smart Esports Hotel Management System Started!         ║
            ║   智慧电竞酒店管理系统启动成功                               ║
            ║                                                           ║
            ║   Swagger UI: http://localhost:8080/api/swagger-ui.html  ║
            ║   API Docs:   http://localhost:8080/api/v3/api-docs      ║
            ║                                                           ║
            ╚═══════════════════════════════════════════════════════════╝
            """);
    }
}

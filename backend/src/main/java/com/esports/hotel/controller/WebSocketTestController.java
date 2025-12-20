package com.esports.hotel.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket测试控制器
 */
@RestController
public class WebSocketTestController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketTestController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 测试发送消息到用户
     */
    @GetMapping("/test/websocket/send/{userId}")
    public String testSendToUser(@PathVariable String userId) {
        try {
            // 创建测试消息
            java.util.Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "TEST");
            message.put("content", "这是一条测试消息");
            message.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("=== 测试WebSocket推送 ===");
            System.out.println("目标用户: " + userId);
            System.out.println("消息内容: " + message);
            
            // 方式1: 使用convertAndSendToUser
            messagingTemplate.convertAndSendToUser(
                userId, 
                "/queue/recruitment", 
                message
            );
            System.out.println("已通过convertAndSendToUser发送");
            
            // 方式2: 直接发送到完整路径
            String destination = "/user/" + userId + "/queue/recruitment";
            messagingTemplate.convertAndSend(destination, message);
            System.out.println("已直接发送到: " + destination);
            
            return "消息已发送到用户: " + userId;
        } catch (Exception e) {
            e.printStackTrace();
            return "发送失败: " + e.getMessage();
        }
    }
}

package com.esports.hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 * 
 * 用于实时推送硬件监控数据到前端
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * /topic - 用于广播消息（如：全店硬件状态）
     * /user - 用于点对点消息（如：个人通知）
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单代理
        registry.enableSimpleBroker("/topic", "/user");
        
        // 客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 点对点消息前缀
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     * 前端通过此端点建立 WebSocket 连接
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域（生产环境需配置具体域名）
                .withSockJS();                 // 启用 SockJS 降级支持
    }
}

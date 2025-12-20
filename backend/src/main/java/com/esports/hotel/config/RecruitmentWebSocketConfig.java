package com.esports.hotel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置 - 招募实时通知
 */
@Configuration
@EnableWebSocketMessageBroker
public class RecruitmentWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单代理，用于广播消息
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用消息前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，支持SockJS备用方案
        registry.addEndpoint("/ws/recruitment")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

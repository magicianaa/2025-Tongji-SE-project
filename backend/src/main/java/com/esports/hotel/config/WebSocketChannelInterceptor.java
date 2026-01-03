package com.esports.hotel.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * WebSocket通道拦截器
 * 用于在STOMP连接时设置用户信息
 */
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // 从WebSocket会话属性中获取guestId
                Object guestId = accessor.getSessionAttributes().get("guestId");
                
                if (guestId != null) {
                    // 设置用户Principal（用于用户特定的消息路由）
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return guestId.toString();
                        }
                    });
                    System.out.println("WebSocket STOMP连接，用户ID: " + guestId);
                }
            }
            
            // 调试：记录所有消息类型
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                System.out.println("用户订阅: " + accessor.getDestination() + 
                    " (SessionID: " + accessor.getSessionId() + 
                    ", User: " + (accessor.getUser() != null ? accessor.getUser().getName() : "null") + ")");
            }
            
            if (StompCommand.MESSAGE.equals(accessor.getCommand())) {
                System.out.println("发送MESSAGE到: " + accessor.getDestination() + 
                    " (User: " + (accessor.getUser() != null ? accessor.getUser().getName() : "null") + ")");
            }
        }
        
        return message;
    }
}

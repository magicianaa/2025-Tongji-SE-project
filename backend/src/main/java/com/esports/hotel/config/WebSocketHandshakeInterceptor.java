package com.esports.hotel.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于在WebSocket连接时传递用户信息
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // 从查询参数中获取guestId（前端连接时需要传递）
            String query = servletRequest.getServletRequest().getQueryString();
            if (query != null && query.contains("guestId=")) {
                String guestId = extractGuestId(query);
                if (guestId != null) {
                    attributes.put("guestId", guestId);
                    System.out.println("WebSocket握手成功，用户ID: " + guestId);
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理（如果需要）
    }

    private String extractGuestId(String query) {
        try {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("guestId=")) {
                    return param.substring("guestId=".length());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

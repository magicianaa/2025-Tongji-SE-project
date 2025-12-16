package com.esports.hotel.util;

import com.esports.hotel.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责生成和解析 Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * 生成密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成普通 Token（登录后获得）
     *
     * @param userId   用户ID
     * @param userType 用户类型（GUEST/STAFF/ADMIN）
     * @return JWT Token
     */
    public String generateToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成客房权限 Token（二次鉴权）
     * 
     * @param userId   用户ID
     * @param recordId 入住记录ID
     * @param roomId   房间ID
     * @return Room-Auth Token
     */
    public String generateRoomAuthToken(Long userId, Long recordId, Long roomId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("recordId", recordId);
        claims.put("roomId", roomId);
        claims.put("authType", "ROOM_ACCESS");
        
        return Jwts.builder()
                .claims(claims)
                .subject("ROOM_AUTH_" + userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRoomAuthExpiration() * 1000))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 从 Token 中获取房间ID（仅 Room-Auth Token）
     */
    public Long getRoomIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object roomId = claims.get("roomId");
        return roomId != null ? Long.valueOf(roomId.toString()) : null;
    }

    /**
     * 从 Token 中获取入住记录ID（仅 Room-Auth Token）
     */
    public Long getRecordIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object recordId = claims.get("recordId");
        return recordId != null ? Long.valueOf(recordId.toString()) : null;
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Header 中提取 Token
     */
    public String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            return authHeader.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }
}

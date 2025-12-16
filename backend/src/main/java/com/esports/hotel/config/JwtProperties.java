package com.esports.hotel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret;

    /**
     * Token 有效期（秒）
     */
    private Long expiration;

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Token Header 名称
     */
    private String headerName = "Authorization";

    /**
     * 二次鉴权 Token 有效期（客房权限，秒）
     */
    private Long roomAuthExpiration;
}

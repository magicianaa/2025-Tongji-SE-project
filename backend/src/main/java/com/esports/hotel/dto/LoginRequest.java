package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名（手机号）不能为空")
    @Schema(description = "用户名/手机号", example = "13800138000")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "abc123")
    private String password;
}

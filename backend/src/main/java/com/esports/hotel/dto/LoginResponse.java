package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "Token类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型（GUEST/STAFF/ADMIN）")
    private String userType;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "会员等级（仅住客）")
    private String memberLevel;

    @Schema(description = "当前积分（仅住客）")
    private Integer currentPoints;
}

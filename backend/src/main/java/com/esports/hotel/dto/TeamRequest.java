package com.esports.hotel.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 战队创建请求DTO
 */
@Data
public class TeamRequest {
    
    @NotBlank(message = "战队名称不能为空")
    private String teamName;
    
    @NotBlank(message = "游戏类型不能为空")
    private String gameType;
}

package com.esports.hotel.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 游戏档案请求DTO
 */
@Data
public class GamingProfileRequest {
    
    @NotBlank(message = "游戏类型不能为空")
    private String gameType;
    
    private String gameAccount;
    
    private String rank;
    
    private String preferredPosition;
    
    private String playStyle;
    
    @NotNull(message = "是否寻找队友状态不能为空")
    private Boolean isLookingForTeam;
}

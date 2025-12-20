package com.esports.hotel.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 招募信息请求DTO
 */
@Data
public class RecruitmentRequest {
    
    @NotBlank(message = "游戏类型不能为空")
    private String gameType;
    
    private String requiredRank;
    
    private String requiredPosition;
    
    @NotBlank(message = "招募描述不能为空")
    private String description;
    
    @NotNull(message = "最大成员数不能为空")
    @Min(value = 2, message = "最大成员数至少为2")
    private Integer maxMembers;
    
    private LocalDateTime expireTime;
}

package com.esports.hotel.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 游戏档案响应DTO
 */
@Data
public class GamingProfileResponse {
    
    private Long profileId;
    
    private Long guestId;
    
    private Long recordId;
    
    private String gameType;
    
    private String gameAccount;
    
    private String rank;
    
    private String preferredPosition;
    
    private String playStyle;
    
    private Boolean isLookingForTeam;
    
    private LocalDateTime createdAt;
    
    // 扩展字段：Guest信息
    private String guestName;
    
    private String roomNumber;
}

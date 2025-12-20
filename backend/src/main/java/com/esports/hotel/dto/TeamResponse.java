package com.esports.hotel.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 战队响应DTO
 */
@Data
public class TeamResponse {
    
    private Long teamId;
    
    private String teamName;
    
    private Long captainId;
    
    private String gameType;
    
    private LocalDateTime createTime;
    
    private LocalDateTime disbandTime;
    
    private String status;
    
    private Integer totalPlaytimeMinutes;
    
    // 扩展字段：队长信息
    private String captainName;
    
    // 战队成员列表
    private List<TeamMemberInfo> members;
    
    @Data
    public static class TeamMemberInfo {
        private Long guestId;
        private String guestName;
        private String rank;
        private String position;
        private LocalDateTime joinTime;
        private String status;
        private Boolean isCaptain; // 是否为队长
    }
}

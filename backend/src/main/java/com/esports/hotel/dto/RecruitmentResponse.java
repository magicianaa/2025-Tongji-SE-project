package com.esports.hotel.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 招募信息响应DTO
 */
@Data
public class RecruitmentResponse {
    
    private Long recruitmentId;
    
    private Long publisherId;
    
    private String gameType;
    
    private String requiredRank;
    
    private String requiredPosition;
    
    private String description;
    
    private Integer maxMembers;
    
    private String status;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime expireTime;
    
    // 扩展字段：发布者信息
    private String publisherName;
    
    private String publisherRank;
    
    private String publisherPosition;
    
    // 当前已应征人数
    private Integer currentApplicants;
}

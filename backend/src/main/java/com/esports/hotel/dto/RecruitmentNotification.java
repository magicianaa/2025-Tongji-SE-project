package com.esports.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 招募通知消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentNotification {
    
    private String type;  // 通知类型：NEW_APPLICATION-新申请, APPLICATION_RESULT-申请结果
    
    private Long recruitmentId;
    
    private Long applicantId;
    
    private String applicantName;
    
    private String applicantRoom;
    
    private String gameType;
    
    private String message;  // 通知消息
    
    private Boolean approved;  // 申请是否被同意（仅申请结果通知使用）
    
    private String timestamp;
}

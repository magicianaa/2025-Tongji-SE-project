package com.esports.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 招募申请响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentApplicationResponse {
    
    /**
     * 申请ID
     */
    private Long applicationId;
    
    /**
     * 招募ID
     */
    private Long recruitmentId;
    
    /**
     * 申请者ID
     */
    private Long applicantId;
    
    /**
     * 申请者姓名
     */
    private String applicantName;
    
    /**
     * 申请者房间号
     */
    private String applicantRoom;
    
    /**
     * 申请者游戏段位（对应招募的游戏类型）
     */
    private String applicantRank;
    
    /**
     * 申请者游戏位置
     */
    private String applicantPosition;
    
    /**
     * 申请状态：PENDING/APPROVED/REJECTED
     */
    private String status;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
}

package com.esports.hotel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务记录视图对象（包含任务详情）
 */
@Data
public class TaskRecordVO {

    private Long taskRecordId;

    private Long taskId;

    private String taskName;

    private Integer rewardPoints;

    private Long guestId;

    private String guestUsername;

    private Long recordId;

    private LocalDateTime submitTime;

    private String proofImageUrl;

    private String proofDescription;

    private String auditStatus;

    private Long auditorId;

    private LocalDateTime auditTime;

    private String rejectReason;
}

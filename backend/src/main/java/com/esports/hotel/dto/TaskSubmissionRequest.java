package com.esports.hotel.dto;

import lombok.Data;

/**
 * 任务提交请求
 */
@Data
public class TaskSubmissionRequest {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 凭证截图URL
     */
    private String proofImageUrl;

    /**
     * 凭证说明
     */
    private String proofDescription;
}

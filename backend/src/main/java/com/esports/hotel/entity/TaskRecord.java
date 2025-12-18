package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务完成记录表实体
 */
@Data
@TableName("tb_task_record")
public class TaskRecord implements Serializable {

    @TableId(value = "task_record_id", type = IdType.AUTO)
    private Long taskRecordId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 住客ID
     */
    private Long guestId;

    /**
     * 入住记录ID
     */
    private Long recordId;

    /**
     * 提交时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;

    /**
     * 凭证截图URL
     */
    private String proofImageUrl;

    /**
     * 凭证说明
     */
    private String proofDescription;

    /**
     * 审核状态：PENDING, APPROVED, REJECTED
     */
    private String auditStatus;

    /**
     * 审核员工ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 拒绝原因
     */
    private String rejectReason;
}

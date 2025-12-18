package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务模板表实体
 */
@Data
@TableName("tb_task")
public class Task implements Serializable {

    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型：AUTO, MANUAL_AUDIT
     */
    private String taskType;

    /**
     * 奖励积分
     */
    private Integer rewardPoints;

    /**
     * 奖励经验值
     */
    private Integer rewardExp;

    /**
     * 完成条件（JSON格式）
     */
    private String conditionConfig;

    /**
     * 是否可重复完成
     */
    private Boolean isRepeatable;

    /**
     * 是否启用
     */
    private Boolean isActive;
}

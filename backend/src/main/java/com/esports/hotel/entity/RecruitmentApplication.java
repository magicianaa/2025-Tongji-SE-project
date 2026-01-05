package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 招募申请实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_recruitment_application")
public class RecruitmentApplication {
    
    @TableId(value = "application_id", type = IdType.AUTO)
    private Long applicationId;
    
    /**
     * 关联招募信息ID
     */
    private Long recruitmentId;
    
    /**
     * 申请者ID（Guest）
     */
    private Long applicantId;
    
    /**
     * 申请状态：PENDING-待处理, APPROVED-已同意, REJECTED-已拒绝
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

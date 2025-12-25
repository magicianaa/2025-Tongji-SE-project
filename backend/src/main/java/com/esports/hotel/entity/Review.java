package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价实体类
 * 对应数据库表: tb_review
 */
@Data
@TableName("tb_review")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID（主键）
     */
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    /**
     * 关联入住记录ID
     */
    @TableField("record_id")
    private Long recordId;

    /**
     * 评价住客ID
     */
    @TableField("guest_id")
    private Long guestId;

    /**
     * 评分（1-5星）
     */
    @TableField("score")
    private Integer score;

    /**
     * 文字评价
     */
    @TableField("comment")
    private String comment;

    /**
     * 评价时间
     */
    @TableField("review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;

    /**
     * 低分标识（虚拟列，由数据库自动维护）
     * ⚠️ 重要：此字段为数据库虚拟列，不参与插入和更新操作
     */
    @TableField(value = "is_low_score", select = true, insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Boolean isLowScore;

    /**
     * 酒店回复
     */
    @TableField("reply")
    private String reply;

    /**
     * 回访状态：NONE(未处理), CONTACTED(已联系), RESOLVED(已解决)
     */
    @TableField("follow_up_status")
    private String followUpStatus;

    /**
     * 回访备注
     */
    @TableField("follow_up_notes")
    private String followUpNotes;

    /**
     * 回访处理人（员工ID）
     */
    @TableField("handler_id")
    private Long handlerId;

    // ========== 以下字段不存储在数据库，仅用于返回给前端 ==========

    /**
     * 评价人姓名（临时字段，脱敏显示）
     */
    @TableField(exist = false)
    private String guestName;

    /**
     * 房间号（临时字段）
     */
    @TableField(exist = false)
    private String roomNo;

    /**
     * 入住时间（临时字段）
     */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkinTime;

    /**
     * 退房时间（临时字段）
     */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkoutTime;
}

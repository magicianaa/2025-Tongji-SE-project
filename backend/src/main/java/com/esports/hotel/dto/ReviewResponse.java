package com.esports.hotel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价响应DTO
 * 包含评价完整信息 + 关联的住客姓名、房间号等
 */
@Data
public class ReviewResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 入住记录ID
     */
    private Long recordId;

    /**
     * 住客ID
     */
    private Long guestId;

    /**
     * 住客姓名
     */
    private String guestName;

    /**
     * 住客手机号
     */
    private String guestPhone;

    /**
     * 房间号
     */
    private String roomNo;

    /**
     * 房型
     */
    private String roomType;

    /**
     * 评分（1-5星）
     */
    private Integer score;

    /**
     * 文字评价
     */
    private String comment;

    /**
     * 评价时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;

    /**
     * 低分标识
     */
    private Boolean isLowScore;

    /**
     * 酒店回复
     */
    private String reply;

    /**
     * 回访状态
     */
    private String followUpStatus;

    /**
     * 回访备注
     */
    private String followUpNotes;

    /**
     * 回访处理人ID
     */
    private Long handlerId;

    /**
     * 回访处理人姓名
     */
    private String handlerName;

    /**
     * 入住时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkInTime;

    /**
     * 退房时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkOutTime;
}

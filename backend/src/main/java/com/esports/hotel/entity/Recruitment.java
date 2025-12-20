package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招募信息实体类
 */
@Data
@TableName("tb_recruitment")
public class Recruitment {

    @TableId(type = IdType.AUTO)
    private Long recruitmentId;

    /**
     * 发布人（住客ID）
     */
    private Long publisherId;

    /**
     * 游戏类型
     */
    private String gameType;

    /**
     * 要求段位
     */
    private String requiredRank;

    /**
     * 缺位置
     */
    private String requiredPosition;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 状态（OPEN-开放中、FULL-已满、CLOSED-已关闭）
     */
    @TableField("`status`")
    private String status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
}

package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临时战队实体类
 */
@Data
@TableName("tb_team")
public class Team {

    @TableId(type = IdType.AUTO)
    private Long teamId;

    /**
     * 战队名称
     */
    private String teamName;

    /**
     * 队长ID
     */
    private Long captainId;

    /**
     * 游戏类型
     */
    private String gameType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 解散时间
     */
    private LocalDateTime disbandTime;

    /**
     * 状态（ACTIVE-活跃、DISBANDED-已解散）
     */
    @TableField("`status`")
    private String status;

    /**
     * 共同游戏时长（分钟）
     */
    private Integer totalPlaytimeMinutes;
}

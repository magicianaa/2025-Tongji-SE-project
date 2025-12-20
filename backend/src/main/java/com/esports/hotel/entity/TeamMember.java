package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 战队成员实体类
 */
@Data
@TableName("tb_team_member")
public class TeamMember {

    @TableId(type = IdType.AUTO)
    private Long memberId;

    /**
     * 战队ID
     */
    private Long teamId;

    /**
     * 住客ID
     */
    private Long guestId;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 离开时间
     */
    private LocalDateTime leaveTime;

    /**
     * 状态（ACTIVE-活跃、LEFT-主动离开、KICKED-被踢出）
     */    @TableField("`status`")    private String status;
}

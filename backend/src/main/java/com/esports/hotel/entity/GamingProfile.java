package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 游戏档案实体类（入住期间有效）
 */
@Data
@TableName("tb_gaming_profile")
public class GamingProfile {

    @TableId(type = IdType.AUTO)
    private Long profileId;

    /**
     * 关联住客ID
     */
    private Long guestId;

    /**
     * 关联本次入住记录
     */
    private Long recordId;

    /**
     * 游戏名称（如：LOL、DOTA2、CSGO、王者荣耀）
     */
    private String gameType;

    /**
     * 游戏账号
     */
    private String gameAccount;

    /**
     * 段位（如：黄金IV、钻石II、传奇）
     */
    @TableField("`rank`")
    private String rank;

    /**
     * 擅长位置（如：辅助、中单、打野）
     */
    private String preferredPosition;

    /**
     * 风格标签（如：稳健型、激进型、支援型）
     */
    private String playStyle;

    /**
     * 是否寻找队友（1-是，0-否）
     */
    private Boolean isLookingForTeam;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

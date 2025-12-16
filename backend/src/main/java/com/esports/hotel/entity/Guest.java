package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 住客信息表实体
 */
@Data
@TableName("tb_guest")
public class Guest implements Serializable {

    @TableId(value = "guest_id", type = IdType.AUTO)
    private Long guestId;

    private Long userId;

    private String realName;

    /**
     * 身份证号（加密存储）
     */
    private String identityCard;

    /**
     * 会员等级：BRONZE, SILVER, GOLD, PLATINUM
     */
    private String memberLevel;

    /**
     * 经验值（用于升级）
     */
    private Integer experiencePoints;

    /**
     * 可用积分余额
     */
    private Integer currentPoints;

    /**
     * 累计入住天数
     */
    private Integer totalCheckinNights;

    private String avatarUrl;
}

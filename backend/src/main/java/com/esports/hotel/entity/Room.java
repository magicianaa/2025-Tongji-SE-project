package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 客房信息表实体
 */
@Data
@TableName("tb_room")
public class Room implements Serializable {

    @TableId(value = "room_id", type = IdType.AUTO)
    private Long roomId;

    /**
     * 房间号（如301）
     */
    private String roomNo;

    /**
     * 房型：SINGLE, DOUBLE, FIVE_PLAYER, VIP
     */
    private String roomType;

    /**
     * 楼层
     */
    private Integer floor;

    /**
     * 房态：VACANT, OCCUPIED, DIRTY, MAINTENANCE
     */
    private String status;

    /**
     * 每小时价格
     */
    private BigDecimal pricePerHour;

    /**
     * 设施配置（JSON格式）
     */
    private String facilityConfig;

    /**
     * 是否高级房型（需高等级会员预订）
     */
    private Boolean isPremium;
}

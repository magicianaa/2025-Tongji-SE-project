package com.esports.hotel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换记录视图对象
 */
@Data
public class PointsRedemptionVO {

    private Long redemptionId;

    private Long guestId;

    private String guestUsername;

    private Long pointsProductId;

    private String productName;

    private Integer pointsCost;

    private LocalDateTime redemptionTime;

    private String status;

    private String fulfillmentNotes;
}

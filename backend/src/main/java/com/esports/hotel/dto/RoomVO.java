package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房间视图对象（包含预订状态）
 */
@Data
@Schema(description = "房间信息")
public class RoomVO {

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房型")
    private String roomType;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "房态")
    private String status;

    @Schema(description = "每小时价格")
    private BigDecimal pricePerDay;

    @Schema(description = "最大容纳人数")
    private Integer maxOccupancy;

    @Schema(description = "当前入住人数")
    private Integer currentOccupancy;

    @Schema(description = "设施配置")
    private String facilityConfig;

    @Schema(description = "是否高级房型")
    private Boolean isPremium;

    @Schema(description = "是否有预订")
    private Boolean hasBooking;

    @Schema(description = "预订ID")
    private Long bookingId;

    @Schema(description = "预订状态")
    private String bookingStatus;
}

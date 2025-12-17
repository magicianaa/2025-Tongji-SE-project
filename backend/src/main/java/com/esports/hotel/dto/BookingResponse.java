package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预订响应DTO
 */
@Data
@Schema(description = "预订响应")
public class BookingResponse {

    @Schema(description = "预订ID")
    private Long bookingId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房型")
    private String roomType;

    @Schema(description = "住客ID")
    private Long guestId;

    @Schema(description = "主住客姓名")
    private String mainGuestName;

    @Schema(description = "预订时间")
    private LocalDateTime bookingTime;

    @Schema(description = "计划入住时间")
    private LocalDateTime plannedCheckin;

    @Schema(description = "计划退房时间")
    private LocalDateTime plannedCheckout;

    @Schema(description = "预订状态")
    private String status;

    @Schema(description = "押金金额")
    private BigDecimal depositAmount;

    @Schema(description = "特殊要求")
    private String specialRequests;
}

package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入住响应")
public class CheckInResponse {

    @Schema(description = "入住记录ID")
    private Long recordId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "入住时间")
    private LocalDateTime actualCheckin;

    @Schema(description = "预期退房时间")
    private LocalDateTime expectedCheckout;

    @Schema(description = "房费（元/小时）")
    private BigDecimal pricePerHour;

    @Schema(description = "客房权限Token（用于访问客房服务）")
    private String roomAuthToken;

    @Schema(description = "提示信息")
    private String message;
}

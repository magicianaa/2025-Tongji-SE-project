package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预订请求DTO
 */
@Data
@Schema(description = "预订请求")
public class BookingRequest {

    @NotNull(message = "房间ID不能为空")
    @Schema(description = "房间ID", example = "1")
    private Long roomId;

    @NotNull(message = "计划入住日期不能为空")
    @Schema(description = "计划入住日期（可以是今天）", example = "2025-12-20")
    private LocalDate plannedCheckin;

    @NotNull(message = "计划退房日期不能为空")
    @Schema(description = "计划退房日期", example = "2025-12-21")
    private LocalDate plannedCheckout;

    @NotBlank(message = "主住客姓名不能为空")
    @Schema(description = "主住客姓名（用于入住验证）", example = "张三")
    private String mainGuestName;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话（用于入住验证）", example = "13800138000")
    private String contactPhone;

    @Schema(description = "特殊要求", example = "需要安静的房间")
    private String specialRequests;
}

package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 办理入住请求 DTO
 */
@Data
@Schema(description = "办理入住请求")
public class CheckInRequest {

    @NotNull(message = "住客ID不能为空")
    @Schema(description = "住客ID", example = "1")
    private Long guestId;

    @NotNull(message = "房间ID不能为空")
    @Schema(description = "房间ID", example = "1")
    private Long roomId;

    @Schema(description = "预订ID（Walk-in时可为空）", example = "1")
    private Long bookingId;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号", example = "320102199001011234")
    private String identityCard;

    @NotNull(message = "预期退房时间不能为空")
    @Future(message = "预期退房时间必须是未来时间")
    @Schema(description = "预期退房时间", example = "2025-12-16T12:00:00")
    private LocalDateTime expectedCheckout;

    @Schema(description = "特殊要求", example = "需要安静房间")
    private String specialRequests;
}

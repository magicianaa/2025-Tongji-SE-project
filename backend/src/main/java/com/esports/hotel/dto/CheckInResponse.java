package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入住登记响应 DTO（支持多人入住）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入住登记响应")
public class CheckInResponse {

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "入住时间")
    private LocalDateTime checkInTime;

    @Schema(description = "预期退房时间")
    private LocalDateTime expectedCheckout;

    @Schema(description = "房费（元/小时）")
    private BigDecimal pricePerHour;

    @Schema(description = "当前入住人数")
    private Integer currentOccupancy;

    @Schema(description = "最大容纳人数")
    private Integer maxOccupancy;

    @Schema(description = "入住住客列表")
    private List<GuestDetail> guests;

    /**
     * 住客详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "住客详情")
    public static class GuestDetail {
        @Schema(description = "入住记录ID")
        private Long recordId;

        @Schema(description = "住客ID")
        private Long guestId;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "真实姓名")
        private String realName;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "是否新注册用户")
        private Boolean isNewUser;

        @Schema(description = "客房权限Token（第一个住客获得）")
        private String roomAuthToken;
    }
}


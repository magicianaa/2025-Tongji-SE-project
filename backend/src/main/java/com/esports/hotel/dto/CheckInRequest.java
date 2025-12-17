package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入住登记请求 DTO（支持多人入住）
 */
@Data
@Schema(description = "入住登记请求")
public class CheckInRequest {

    @NotBlank(message = "房间号不能为空")
    @Schema(description = "房间号", example = "201")
    private String roomNo;

    @Schema(description = "预期退房时间")
    private LocalDateTime expectedCheckout;

    @NotEmpty(message = "住客信息不能为空")
    @Schema(description = "住客信息列表")
    private List<GuestInfo> guests;

    /**
     * 住客信息内部类
     */
    @Data
    @Schema(description = "住客信息")
    public static class GuestInfo {
        
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Schema(description = "手机号", example = "13800138000")
        private String phone;

        @NotBlank(message = "真实姓名不能为空")
        @Schema(description = "真实姓名", example = "张三")
        private String realName;

        @NotBlank(message = "身份证号不能为空")
        @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", 
                 message = "身份证号格式不正确")
        @Schema(description = "身份证号", example = "110101199001011234")
        private String idCard;
    }
}


package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退房账单响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "退房账单")
public class CheckOutResponse {

    @Schema(description = "入住记录ID")
    private Long recordId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "入住时长（小时）")
    private Long stayHours;

    @Schema(description = "房费小计")
    private BigDecimal roomFee;

    @Schema(description = "POS挂账金额")
    private BigDecimal posCharges;

    @Schema(description = "赔偿金")
    private BigDecimal damageCompensation;

    @Schema(description = "积分抵扣金额")
    private BigDecimal pointsDeduction;

    @Schema(description = "最终应付金额")
    private BigDecimal finalAmount;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "消费明细")
    private List<BillItem> billItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "账单明细项")
    public static class BillItem {
        @Schema(description = "项目名称")
        private String itemName;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "类型")
        private String type;
    }
}

package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 清扫请求DTO
 */
@Data
@Schema(description = "清扫请求")
public class CleaningRequest {

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "清扫类型: CHECKOUT(退房清扫), DAILY(日常清扫), DEEP(深度清扫)")
    private String cleaningType;

    @Schema(description = "消耗物资列表")
    private List<SupplyItem> supplies;

    @Schema(description = "备注")
    private String notes;

    @Data
    @Schema(description = "物资消耗项")
    public static class SupplyItem {
        @Schema(description = "物资ID")
        private Long productId;

        @Schema(description = "消耗数量")
        private Integer quantity;
    }
}

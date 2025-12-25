package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 进货请求DTO
 */
@Data
@Schema(description = "进货请求")
public class ProcurementRequest {

    @Schema(description = "商品ID", required = true)
    private Long productId;

    @Schema(description = "进货数量", required = true)
    private Integer quantity;

    @Schema(description = "进货单价", required = true)
    private BigDecimal unitCost;

    @Schema(description = "供应商")
    private String supplier;

    @Schema(description = "备注")
    private String notes;
}

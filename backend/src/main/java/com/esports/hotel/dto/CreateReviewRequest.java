package com.esports.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 创建评价请求DTO
 */
@Data
@Schema(description = "创建评价请求")
public class CreateReviewRequest {

    @Schema(description = "入住记录ID")
    @NotNull(message = "入住记录ID不能为空")
    private Long recordId;

    @Schema(description = "评分（1-5星）")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1星")
    @Max(value = 5, message = "评分最大为5星")
    private Integer score;

    @Schema(description = "文字评价")
    @Size(max = 1000, message = "评价内容不能超过1000字")
    private String comment;
}

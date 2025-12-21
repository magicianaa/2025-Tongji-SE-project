package com.esports.hotel.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 提交评价请求DTO
 */
@Data
public class ReviewSubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 入住记录ID
     */
    @NotNull(message = "入住记录ID不能为空")
    private Long recordId;

    /**
     * 评分（1-5星）
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能低于1星")
    @Max(value = 5, message = "评分不能超过5星")
    private Integer score;

    /**
     * 文字评价（可选）
     */
    @Size(max = 500, message = "评价内容不能超过500字")
    private String comment;
}

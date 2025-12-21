package com.esports.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 回访登记请求DTO
 */
@Data
public class FollowUpRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 回访状态: CONTACTED(已联系) 或 RESOLVED(已解决)
     */
    @NotBlank(message = "回访状态不能为空")
    private String followUpStatus;

    /**
     * 回访备注（必填）
     */
    @NotBlank(message = "回访备注不能为空")
    private String followUpNotes;

    /**
     * 酒店回复（可选）
     */
    private String reply;
}

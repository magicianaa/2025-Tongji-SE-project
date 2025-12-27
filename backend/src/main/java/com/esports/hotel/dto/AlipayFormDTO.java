package com.esports.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付宝支付表单DTO，包含商户订单号和HTML表单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlipayFormDTO {
    /**
     * 商户订单号（用于轮询查询支付状态）
     */
    private String outTradeNo;
    
    /**
     * 支付表单HTML
     */
    private String formHtml;
}

package com.esports.hotel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置（沙箱环境）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    /** 应用 AppId */
    private String appId;

    /** 支付宝网关 */
    private String gateway;

    /** 商户应用私钥 */
    private String merchantPrivateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 签名算法，默认 RSA2 */
    private String signType = "RSA2";

    /** 编码，默认 UTF-8 */
    private String charset = "UTF-8";

    /** 数据格式，默认 json */
    private String format = "json";

    /** 异步通知地址 */
    private String notifyUrl;

    /** 前端回跳地址 */
    private String returnUrl;
}

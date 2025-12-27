package com.esports.hotel.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.esports.hotel.config.AlipayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝下单与验签服务（沙箱环境）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayProperties alipayProperties;

    private AlipayClient buildClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGateway(),
                alipayProperties.getAppId(),
                alipayProperties.getMerchantPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType()
        );
    }

    /**
     * 生成电脑网站支付表单 HTML。
     */
    public String createPageOrder(String outTradeNo, String subject, String totalAmount,
                                  String notifyUrl, String returnUrl) {
        try {
            AlipayClient client = buildClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            String bizContent = "{" +
                    "\"out_trade_no\":\"" + outTradeNo + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                    "\"total_amount\":\"" + totalAmount + "\"," +
                    "\"subject\":\"" + subject + "\"" +
                    "}";
            request.setBizContent(bizContent);
            return client.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付表单失败: outTradeNo={}, subject={}, amount={}", outTradeNo, subject, totalAmount, e);
            throw new RuntimeException("创建支付宝支付表单失败" + e.getErrMsg(), e);
        }
    }

    /**
     * 验证支付宝回调签名。
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签失败", e);
            return false;
        }
    }

    /**
     * 查询支付宝交易状态。
     * @param outTradeNo 商户订单号
     * @return 交易状态：TRADE_SUCCESS-成功, WAIT_BUYER_PAY-等待付款, TRADE_CLOSED-关闭, null-查询失败
     */
    public String queryTradeStatus(String outTradeNo) {
        try {
            AlipayClient client = buildClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            String bizContent = "{\"out_trade_no\":\"" + outTradeNo + "\"}";
            request.setBizContent(bizContent);
            
            AlipayTradeQueryResponse response = client.execute(request);
            if (response.isSuccess()) {
                log.info("支付宝交易查询成功: outTradeNo={}, tradeStatus={}", outTradeNo, response.getTradeStatus());
                return response.getTradeStatus();
            } else {
                log.warn("支付宝交易查询失败: outTradeNo={}, code={}, msg={}", 
                        outTradeNo, response.getCode(), response.getMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝交易查询异常: outTradeNo={}", outTradeNo, e);
            return null;
        }
    }
}

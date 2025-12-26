package com.esports.hotel.controller;

import com.esports.hotel.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pay/alipay")
@RequiredArgsConstructor
@Tag(name = "支付宝支付", description = "支付宝支付与回调")
public class PayController {

    private final PaymentService paymentService;

    @Operation(summary = "预订订金-支付宝电脑网站支付")
    @PostMapping("/deposit/{bookingId}")
    public void createDepositPay(@PathVariable Long bookingId, HttpServletResponse response) throws IOException {
        String form = paymentService.createDepositPayForm(bookingId);
        writeHtml(response, form);
    }

    @Operation(summary = "账单清付-支付宝电脑网站支付")
    @PostMapping("/bill/{recordId}")
    public void createBillPay(@PathVariable Long recordId, HttpServletResponse response) throws IOException {
        String form = paymentService.createBillPayForm(recordId);
        writeHtml(response, form);
    }

    @Operation(summary = "支付宝异步通知")
    @PostMapping("/notify")
    public String notifyAlipay(@RequestParam Map<String, String> params) {
        log.info("======= 收到支付宝异步回调通知 =======");
        log.info("回调参数: {}", params);
        boolean success = paymentService.handleAlipayNotify(new HashMap<>(params));
        log.info("回调处理结果: {}", success ? "成功" : "失败");
        return success ? "success" : "fail";
    }

    @Operation(summary = "支付宝前端回跳")
    @GetMapping("/return")
    public String returnAlipay() {
        return "支付已提交，请返回系统查看支付结果";
    }

    private void writeHtml(HttpServletResponse response, String formHtml) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(formHtml);
        response.getWriter().flush();
    }
}

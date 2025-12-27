package com.esports.hotel.service;

import com.esports.hotel.common.BusinessException;
import com.esports.hotel.config.AlipayProperties;
import com.esports.hotel.dto.AlipayFormDTO;
import com.esports.hotel.dto.BillDetailDTO;
import com.esports.hotel.entity.Booking;
import com.esports.hotel.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 支付聚合服务：生成支付宝支付表单并处理回调。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PREFIX_DEPOSIT = "DEP-";
    private static final String PREFIX_BILL = "BILL-";

    private final AlipayService alipayService;
    private final AlipayProperties alipayProperties;
    private final BookingMapper bookingMapper;
    private final BookingService bookingService;
    private final BillingService billingService;

    public AlipayFormDTO createDepositPayForm(Long bookingId) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }
        if (!"UNPAID".equals(booking.getDepositPaymentStatus())) {
            throw new BusinessException("订金已支付或已退款，无法再次下单");
        }
        BigDecimal amount = booking.getDepositAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("订金金额无效");
        }
        String outTradeNo = PREFIX_DEPOSIT + bookingId + "-" + System.currentTimeMillis();
        String subject = "预订订金支付-" + bookingId;
        String formHtml = alipayService.createPageOrder(
                outTradeNo,
                subject,
                amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                alipayProperties.getNotifyUrl(),
                alipayProperties.getReturnUrl()
        );
        return new AlipayFormDTO(outTradeNo, formHtml);
    }

    public AlipayFormDTO createBillPayForm(Long recordId) {
        BillDetailDTO bill = billingService.getBillDetail(recordId);
        BigDecimal amount = bill.getUnpaidAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("账单已结清，无需支付");
        }
        String outTradeNo = PREFIX_BILL + recordId + "-" + System.currentTimeMillis();
        String subject = "账单清付-入住记录" + recordId;
        String formHtml = alipayService.createPageOrder(
                outTradeNo,
                subject,
                amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                alipayProperties.getNotifyUrl(),
                alipayProperties.getReturnUrl()
        );
        return new AlipayFormDTO(outTradeNo, formHtml);
    }

    public boolean handleAlipayNotify(Map<String, String> params) {
        if (!alipayService.verifySignature(params)) {
            log.warn("支付宝回调验签失败, params={}", params);
            return false;
        }
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        if (!"TRADE_SUCCESS".equalsIgnoreCase(tradeStatus) &&
            !"TRADE_FINISHED".equalsIgnoreCase(tradeStatus)) {
            log.info("支付宝回调，非成功状态: status={}, outTradeNo={}", tradeStatus, outTradeNo);
            return true;
        }
        if (outTradeNo == null) {
            log.warn("支付宝回调缺少 out_trade_no");
            return false;
        }
        try {
            if (outTradeNo.startsWith(PREFIX_DEPOSIT)) {
                Long bookingId = parseId(outTradeNo, PREFIX_DEPOSIT);
                if (bookingId != null) {
                    bookingService.payDeposit(bookingId, "ALIPAY");
                }
            } else if (outTradeNo.startsWith(PREFIX_BILL)) {
                Long recordId = parseId(outTradeNo, PREFIX_BILL);
                if (recordId != null) {
                    billingService.settleBill(recordId, "ALIPAY");
                }
            } else {
                log.warn("无法识别的订单前缀: {}", outTradeNo);
            }
            return true;
        } catch (Exception ex) {
            log.error("处理支付宝回调失败: outTradeNo={}", outTradeNo, ex);
            return false;
        }
    }

    private Long parseId(String outTradeNo, String prefix) {
        try {
            String body = outTradeNo.substring(prefix.length());
            String idPart = body.split("-")[0];
            return Long.parseLong(idPart);
        } catch (Exception e) {
            log.warn("解析 out_trade_no 失败: {}", outTradeNo, e);
            return null;
        }
    }

    /**
     * 生成订金支付的商户订单号
     */
    public String generateDepositOutTradeNo(Long bookingId) {
        return PREFIX_DEPOSIT + bookingId + "-" + System.currentTimeMillis();
    }

    /**
     * 生成账单支付的商户订单号
     */
    public String generateBillOutTradeNo(Long recordId) {
        return PREFIX_BILL + recordId + "-" + System.currentTimeMillis();
    }

    /**
     * 主动查询支付宝交易状态并处理业务（用于轮询场景）
     * @param outTradeNo 商户订单号
     * @return 交易状态
     */
    public String queryAndProcessPayment(String outTradeNo) {
        String tradeStatus = alipayService.queryTradeStatus(outTradeNo);
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 支付成功，处理业务
            try {
                if (outTradeNo.startsWith(PREFIX_DEPOSIT)) {
                    Long bookingId = parseId(outTradeNo, PREFIX_DEPOSIT);
                    if (bookingId != null) {
                        Booking booking = bookingMapper.selectById(bookingId);
                        // 只有未支付的才处理，避免重复
                        if (booking != null && "UNPAID".equals(booking.getDepositPaymentStatus())) {
                            bookingService.payDeposit(bookingId, "ALIPAY");
                            log.info("轮询检测到支付成功，已处理订金: bookingId={}", bookingId);
                        }
                    }
                } else if (outTradeNo.startsWith(PREFIX_BILL)) {
                    Long recordId = parseId(outTradeNo, PREFIX_BILL);
                    if (recordId != null) {
                        BillDetailDTO bill = billingService.getBillDetail(recordId);
                        // 只有有未付金额才处理
                        if (bill.getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                            billingService.settleBill(recordId, "ALIPAY");
                            log.info("轮询检测到支付成功，已处理账单: recordId={}", recordId);
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("轮询处理支付失败: outTradeNo={}", outTradeNo, ex);
            }
        }
        return tradeStatus;
    }
}

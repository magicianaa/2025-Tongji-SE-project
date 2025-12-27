import request from '@/utils/request'

/**
 * 创建预订订金支付宝支付
 * @returns { outTradeNo: string, formHtml: string }
 */
export function createDepositAlipay(bookingId) {
  return request.post(`/pay/alipay/deposit/${bookingId}`)
}

/**
 * 创建账单清付支付宝支付
 * @returns { outTradeNo: string, formHtml: string }
 */
export function createBillAlipay(recordId) {
  return request.post(`/pay/alipay/bill/${recordId}`)
}

/**
 * 查询支付宝交易状态
 * @returns { outTradeNo: string, tradeStatus: string, success: boolean }
 */
export function queryAlipayStatus(outTradeNo) {
  return request.get(`/pay/alipay/query`, {
    params: { outTradeNo }
  })
}

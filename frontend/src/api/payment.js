import axios from 'axios'

/**
 * 创建预订订金支付宝支付
 * @returns { outTradeNo: string, formHtml: string }
 */
export function createDepositAlipay(bookingId) {
  return axios.post(`/api/pay/alipay/deposit/${bookingId}`, null, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 创建账单清付支付宝支付
 * @returns { outTradeNo: string, formHtml: string }
 */
export function createBillAlipay(recordId) {
  return axios.post(`/api/pay/alipay/bill/${recordId}`, null, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

/**
 * 查询支付宝交易状态
 * @returns { outTradeNo: string, tradeStatus: string, success: boolean }
 */
export function queryAlipayStatus(outTradeNo) {
  return axios.get(`/api/pay/alipay/query`, {
    params: { outTradeNo },
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
}

import axios from 'axios'

/**
 * 创建预订订金支付宝支付
 */
export function createDepositAlipay(bookingId) {
  return axios.post(`/api/pay/alipay/deposit/${bookingId}`, null, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    responseType: 'text'
  })
}

/**
 * 创建账单清付支付宝支付
 */
export function createBillAlipay(recordId) {
  return axios.post(`/api/pay/alipay/bill/${recordId}`, null, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    responseType: 'text'
  })
}

import request from '@/utils/request'

/**
 * 创建订单
 */
export function createOrder(data) {
  return request({
    url: '/pos-orders',
    method: 'post',
    data
  })
}

/**
 * 标记订单为已配送
 */
export function deliverOrder(orderId) {
  return request({
    url: `/pos-orders/${orderId}/deliver`,
    method: 'put'
  })
}

/**
 * 归还租赁设备
 */
export function returnRental(orderId) {
  return request({
    url: `/pos-orders/${orderId}/return`,
    method: 'put'
  })
}

/**
 * 获取我的订单列表
 */
export function getMyOrders(recordId) {
  return request({
    url: '/pos-orders/my-orders',
    method: 'get',
    params: { recordId }
  })
}

/**
 * 获取待配送订单
 */
export function getPendingOrders() {
  return request({
    url: '/pos-orders/pending',
    method: 'get'
  })
}

/**
 * 获取所有订单
 */
export function getAllOrders(params) {
  return request({
    url: '/pos-orders',
    method: 'get',
    params
  })
}

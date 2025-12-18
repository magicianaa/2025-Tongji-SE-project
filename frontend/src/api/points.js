import request from '@/utils/request'

/**
 * 积分管理API
 */

// 查询积分余额
export function getPointsBalance() {
  return request({
    url: '/api/points/balance',
    method: 'get'
  })
}

// 查询积分流水
export function getPointsTransactions(limit = 50) {
  return request({
    url: '/api/points/transactions',
    method: 'get',
    params: { limit }
  })
}

import request from '@/utils/request'

/**
 * 积分商城API
 */

// ==================== 商品管理（员工端） ====================

// 获取所有积分商品
export function getPointsProducts(availableOnly = true) {
  return request({
    url: '/api/points-shop/products',
    method: 'get',
    params: { availableOnly }
  })
}

// 上架积分商品
export function createPointsProduct(data) {
  return request({
    url: '/api/points-shop/products',
    method: 'post',
    data
  })
}

// 更新积分商品
export function updatePointsProduct(productId, data) {
  return request({
    url: `/api/points-shop/products/${productId}`,
    method: 'put',
    data
  })
}

// 下架积分商品
export function deletePointsProduct(productId) {
  return request({
    url: `/api/points-shop/products/${productId}`,
    method: 'delete'
  })
}

// ==================== 商品兑换（住客端） ====================

// 兑换积分商品
export function redeemProduct(pointsProductId) {
  return request({
    url: '/api/points-shop/redeem',
    method: 'post',
    params: { pointsProductId }
  })
}

// 获取我的兑换记录
export function getMyRedemptions() {
  return request({
    url: '/api/points-shop/my-redemptions',
    method: 'get'
  })
}

// ==================== 兑换订单管理（员工端） ====================

// 获取所有兑换订单
export function getAllRedemptions(status = null) {
  return request({
    url: '/api/points-shop/redemptions',
    method: 'get',
    params: status ? { status } : {}
  })
}

// 更新兑换订单状态
export function updateRedemptionStatus(redemptionId, status, notes = null) {
  return request({
    url: `/api/points-shop/redemptions/${redemptionId}/status`,
    method: 'put',
    params: { status, notes }
  })
}

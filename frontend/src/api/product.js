import request from '@/utils/request'

/**
 * 获取商品列表（分页）
 */
export function getProducts(params) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

/**
 * 获取所有可用商品（不分页）
 */
export function getAvailableProducts() {
  return request({
    url: '/products/available',
    method: 'get'
  })
}

/**
 * 根据ID获取商品
 */
export function getProductById(productId) {
  return request({
    url: `/products/${productId}`,
    method: 'get'
  })
}

/**
 * 添加商品
 */
export function addProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 */
export function updateProduct(productId, data) {
  return request({
    url: `/products/${productId}`,
    method: 'put',
    data
  })
}

/**
 * 删除商品
 */
export function deleteProduct(productId) {
  return request({
    url: `/products/${productId}`,
    method: 'delete'
  })
}

/**
 * 更新库存
 */
export function updateStock(productId, quantity) {
  return request({
    url: `/products/${productId}/stock`,
    method: 'put',
    params: { quantity }
  })
}

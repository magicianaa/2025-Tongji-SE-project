import request from '@/utils/request'

/**
 * 进货管理API
 */

// 获取进货记录列表
export function getProcurementList(params) {
  return request({
    url: '/procurement/list',
    method: 'get',
    params
  })
}

// 创建进货记录
export function createProcurement(data) {
  return request({
    url: '/procurement',
    method: 'post',
    data
  })
}

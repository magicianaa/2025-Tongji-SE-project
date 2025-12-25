import request from '@/utils/request'

/**
 * 清扫管理API
 */

// 获取清扫记录列表
export function getCleaningRecords(params) {
  return request({
    url: '/cleaning/list',
    method: 'get',
    params
  })
}

// 创建清扫记录（完成清扫）
export function completeCleaning(data) {
  return request({
    url: '/cleaning/complete',
    method: 'post',
    data
  })
}

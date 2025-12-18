import request from '@/utils/request'

/**
 * 获取所有房间硬件状态
 */
export const getHardwareStatus = () => {
  return request({
    url: '/hardware/status',
    method: 'get'
  })
}

/**
 * 获取指定房间硬件状态
 */
export const getRoomHardwareStatus = (roomId) => {
  return request({
    url: `/hardware/status/${roomId}`,
    method: 'get'
  })
}

/**
 * 触发硬件故障（测试用）
 */
export const triggerFailure = (roomId) => {
  return request({
    url: `/hardware/trigger-failure/${roomId}`,
    method: 'post'
  })
}

/**
 * 获取告警日志
 */
export const getAlertLogs = (params) => {
  return request({
    url: '/hardware/alerts',
    method: 'get',
    params
  })
}

/**
 * 处理告警
 */
export const handleAlert = (alertId) => {
  return request({
    url: `/hardware/alerts/${alertId}/handle`,
    method: 'post'
  })
}

/**
 * 获取维修工单列表
 */
export const getMaintenanceTickets = (params) => {
  return request({
    url: '/hardware/tickets',
    method: 'get',
    params
  })
}

/**
 * 创建维修工单（报修）
 */
export const createTicket = (data) => {
  return request({
    url: '/hardware/tickets',
    method: 'post',
    params: data
  })
}

/**
 * 获取工单详情
 */
export const getTicketDetail = (ticketId) => {
  return request({
    url: `/hardware/tickets/${ticketId}`,
    method: 'get'
  })
}

/**
 * 更新工单状态
 */
export const updateTicketStatus = (ticketId, data) => {
  return request({
    url: `/hardware/tickets/${ticketId}/status`,
    method: 'put',
    params: data
  })
}

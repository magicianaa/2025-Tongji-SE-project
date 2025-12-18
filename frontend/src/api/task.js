import request from '@/utils/request'

/**
 * 任务管理API
 */

// ==================== 任务管理（员工端） ====================

// 获取所有任务
export function getTasks(activeOnly = true) {
  return request({
    url: '/api/tasks',
    method: 'get',
    params: { activeOnly }
  })
}

// 发布新任务
export function createTask(data) {
  return request({
    url: '/api/tasks',
    method: 'post',
    data
  })
}

// 更新任务
export function updateTask(taskId, data) {
  return request({
    url: `/api/tasks/${taskId}`,
    method: 'put',
    data
  })
}

// 删除/下架任务
export function deleteTask(taskId) {
  return request({
    url: `/api/tasks/${taskId}`,
    method: 'delete'
  })
}

// ==================== 任务提交（住客端） ====================

// 提交任务完成凭证
export function submitTask(data) {
  return request({
    url: '/api/tasks/submit',
    method: 'post',
    data
  })
}

// 获取我的任务记录
export function getMyTaskRecords() {
  return request({
    url: '/api/tasks/my-records',
    method: 'get'
  })
}

// ==================== 任务审核（员工端） ====================

// 获取待审核任务列表
export function getTaskRecords(auditStatus = null) {
  return request({
    url: '/api/tasks/records',
    method: 'get',
    params: auditStatus ? { auditStatus } : {}
  })
}

// 审核任务
export function auditTask(taskRecordId, auditorId, approved, rejectReason = null) {
  return request({
    url: `/api/tasks/records/${taskRecordId}/audit`,
    method: 'put',
    params: { auditorId, approved, rejectReason }
  })
}

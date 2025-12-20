/**
 * Phase 3: 社交匹配子系统 - 招募 & 战队 API
 */
import request from '@/utils/request'

// ==================== 招募管理 ====================

/**
 * 发布招募信息
 */
export function publishRecruitment(data) {
  return request({
    url: '/recruitments',
    method: 'post',
    data
  })
}

/**
 * 查询招募列表（分页+筛选）
 */
export function searchRecruitments(params) {
  return request({
    url: '/recruitments/search',
    method: 'get',
    params // { gameType?, rank?, position?, page, size }
  })
}

/**
 * 获取招募详情
 */
export function getRecruitment(recruitmentId) {
  return request({
    url: `/recruitments/${recruitmentId}`,
    method: 'get'
  })
}

/**
 * 关闭招募
 */
export function closeRecruitment(recruitmentId) {
  return request({
    url: `/recruitments/${recruitmentId}/close`,
    method: 'put'
  })
}

/**
 * 删除招募
 */
export function deleteRecruitment(recruitmentId) {
  return request({
    url: `/recruitments/${recruitmentId}`,
    method: 'delete'
  })
}

/**
 * 获取我发布的招募
 */
export function getMyRecruitments() {
  return request({
    url: '/recruitments/my',
    method: 'get'
  })
}

/**
 * 申请加入招募
 */
export function applyToRecruitment(recruitmentId) {
  return request({
    url: `/recruitments/${recruitmentId}/apply`,
    method: 'post'
  })
}

/**
 * 同意申请
 */
export function approveApplication(recruitmentId, applicantId) {
  return request({
    url: `/recruitments/${recruitmentId}/approve`,
    method: 'post',
    params: { applicantId }
  })
}

/**
 * 拒绝申请
 */
export function rejectApplication(recruitmentId, applicantId) {
  return request({
    url: `/recruitments/${recruitmentId}/reject`,
    method: 'post',
    params: { applicantId }
  })
}

// ==================== 战队管理 ====================

/**
 * 创建战队
 */
export function createTeam(data) {
  return request({
    url: '/teams',
    method: 'post',
    data
  })
}

/**
 * 加入战队
 */
export function joinTeam(teamId) {
  return request({
    url: `/teams/${teamId}/join`,
    method: 'post'
  })
}

/**
 * 离开战队
 */
export function leaveTeam(teamId) {
  return request({
    url: `/teams/${teamId}/leave`,
    method: 'post'
  })
}

/**
 * 踢出成员
 */
export function kickMember(teamId, memberId) {
  return request({
    url: `/teams/${teamId}/kick`,
    method: 'post',
    params: { memberId }
  })
}

/**
 * 解散战队
 */
export function disbandTeam(teamId) {
  return request({
    url: `/teams/${teamId}`,
    method: 'delete'
  })
}

/**
 * 获取战队详情
 */
export function getTeam(teamId) {
  return request({
    url: `/teams/${teamId}`,
    method: 'get'
  })
}

/**
 * 获取我的战队
 */
export function getMyTeam() {
  return request({
    url: '/teams/my',
    method: 'get'
  })
}

/**
 * 更新战队游戏时长
 */
export function updatePlaytime(teamId, additionalMinutes) {
  return request({
    url: `/teams/${teamId}/playtime`,
    method: 'put',
    params: { additionalMinutes }
  })
}

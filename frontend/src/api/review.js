/**
 * 评价管理 API
 */
import request from '@/utils/request'

// ==================== 住客端 ====================

/**
 * 获取我的入住记录（用于评价）
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Promise}
 */
export function getMyCheckinHistory(pageNum = 1, pageSize = 10) {
  return request({
    url: '/reviews/my-checkin-history',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 创建评价（新接口）
 * @param {Object} data - 评价数据
 * @param {number} data.recordId - 入住记录ID
 * @param {number} data.score - 评分(1-5)
 * @param {string} data.comment - 评价内容
 * @returns {Promise}
 */
export function createReview(data) {
  return request({
    url: '/reviews/create',
    method: 'post',
    data
  })
}

/**
 * 获取房间评价列表
 * @param {number} roomId - 房间ID
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Promise}
 */
export function getRoomReviews(roomId, pageNum = 1, pageSize = 10) {
  return request({
    url: `/reviews/room/${roomId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取房间平均评分
 * @param {number} roomId - 房间ID
 * @returns {Promise}
 */
export function getAverageScore(roomId) {
  return request({
    url: `/reviews/room/${roomId}/average-score`,
    method: 'get'
  })
}

/**
 * 提交评价
 */
export function submitReview(data) {
  return request({
    url: '/reviews',
    method: 'post',
    params: { guestId: data.guestId },
    data: {
      recordId: data.recordId,
      score: data.score,
      comment: data.comment
    }
  })
}

/**
 * 获取我的评价记录
 */
export function getMyReviews(guestId) {
  return request({
    url: '/reviews/my',
    method: 'get',
    params: { guestId }
  })
}

/**
 * 检查入住记录是否已评价
 */
export function hasReviewed(recordId) {
  return request({
    url: '/reviews/check',
    method: 'get',
    params: { recordId }
  })
}

/**
 * 更新评价
 */
export function updateReview(reviewId, data) {
  return request({
    url: `/reviews/${reviewId}`,
    method: 'put',
    data: {
      recordId: data.recordId,
      score: data.score,
      comment: data.comment
    }
  })
}

/**
 * 删除评价
 */
export function deleteReview(reviewId) {
  return request({
    url: `/reviews/${reviewId}`,
    method: 'delete'
  })
}

// ==================== 管理端 ====================

/**
 * 获取评价列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {boolean} params.lowScoreOnly - 仅显示低分评价
 * @param {string} params.followUpStatus - 回访状态筛选
 */
export function getReviews(params) {
  return request({
    url: '/reviews',
    method: 'get',
    params
  })
}

/**
 * 获取低分预警列表
 */
export function getLowScoreReviews() {
  return request({
    url: '/reviews/low-score',
    method: 'get'
  })
}

/**
 * 获取评价详情
 */
export function getReviewById(reviewId) {
  return request({
    url: `/reviews/${reviewId}`,
    method: 'get'
  })
}

/**
 * 更新回访信息
 * @param {number} reviewId - 评价ID
 * @param {number} handlerId - 处理人ID
 * @param {Object} data - 回访数据
 * @param {string} data.followUpStatus - 回访状态（CONTACTED/RESOLVED）
 * @param {string} data.followUpNotes - 回访备注
 * @param {string} data.reply - 酒店回复（可选）
 */
export function updateFollowUp(reviewId, handlerId, data) {
  return request({
    url: `/reviews/${reviewId}/follow-up`,
    method: 'put',
    params: { handlerId },
    data
  })
}

/**
 * 回复评价
 */
export function replyReview(reviewId, reply) {
  return request({
    url: `/reviews/${reviewId}/reply`,
    method: 'put',
    data: reply,
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}

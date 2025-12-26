import request from '@/utils/request'

/**
 * 报表与决策支持 API
 */

// ==================== 运营看板 ====================

/**
 * 获取运营看板数据
 */
export function getDashboardStats() {
  return request({
    url: '/reports/dashboard',
    method: 'get'
  })
}

// ==================== 财务报表 ====================

/**
 * 获取财务日报
 * @param {string} date - 日期，格式：yyyy-MM-dd
 */
export function getDailyReport(date) {
  return request({
    url: '/reports/financial/daily',
    method: 'get',
    params: { date }
  })
}

/**
 * 获取财务月报
 * @param {number} year - 年份
 * @param {number} month - 月份（1-12）
 */
export function getMonthlyReport(year, month) {
  return request({
    url: '/reports/financial/monthly',
    method: 'get',
    params: { year, month }
  })
}

/**
 * 导出财务报表
 * @param {string} reportType - 报表类型：DAILY / MONTHLY
 * @param {string} date - 日期，格式：yyyy-MM-dd
 */
export function exportFinancialReport(reportType, date) {
  return request({
    url: '/reports/financial/export',
    method: 'get',
    params: { reportType, date },
    responseType: 'blob' // 重要：设置响应类型为blob
  })
}

// ==================== 硬件损耗分析 ====================

/**
 * 获取硬件损耗分析
 * @param {number} days - 分析天数，默认30天
 */
export function getHardwareAnalysis(days = 30) {
  return request({
    url: '/reports/hardware/analysis',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取采购建议清单
 * @param {number} days - 分析天数，默认30天
 */
export function getPurchaseRecommendations(days = 30) {
  return request({
    url: '/reports/hardware/purchase-recommendations',
    method: 'get',
    params: { days }
  })
}

// ==================== AI财报分析 ====================

/**
 * 生成AI财报分析
 * @param {string} reportType - 报表类型：DAILY / MONTHLY
 * @param {string} date - 日期，格式：yyyy-MM-dd
 */
export function generateAIAnalysis(reportType, date) {
  return request({
    url: '/reports/financial/ai-analysis',
    method: 'get',
    params: { reportType, date },
    timeout: 60000 // AI生成需要较长时间，设置60秒超时
  })
}

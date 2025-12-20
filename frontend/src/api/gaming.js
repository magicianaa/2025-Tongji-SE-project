/**
 * Phase 3: 社交匹配子系统 - 游戏档案 & 匹配推荐 API
 */
import request from '@/utils/request'

// ==================== 游戏档案管理 ====================

/**
 * 创建或更新游戏档案
 */
export function createOrUpdateProfile(data) {
  return request({
    url: '/gaming-profiles',
    method: 'post',
    data
  })
}

/**
 * 获取游戏档案详情
 */
export function getProfile(profileId) {
  return request({
    url: `/gaming-profiles/${profileId}`,
    method: 'get'
  })
}

/**
 * 获取当前游戏档案（当前入住期间）
 */
export function getCurrentProfile(gameType) {
  return request({
    url: '/gaming-profiles/current',
    method: 'get',
    params: { gameType }
  })
}

/**
 * 删除游戏档案
 */
export function deleteProfile(profileId) {
  return request({
    url: `/gaming-profiles/${profileId}`,
    method: 'delete'
  })
}

/**
 * 获取在线玩家列表（在线大厅）
 */
export function getOnlinePlayers(params) {
  return request({
    url: '/gaming-profiles/online',
    method: 'get',
    params // { gameType?, rank? }
  })
}

// ==================== 匹配推荐 ====================

/**
 * 推荐合适的队友
 */
export function recommendTeammates(gameType, limit = 10) {
  return request({
    url: '/matching/recommend',
    method: 'get',
    params: { gameType, limit }
  })
}

// ==================== 游戏类型和段位配置 ====================

export const GAME_TYPES = [
  { label: '英雄联盟', value: 'LOL' },
  { label: 'DOTA2', value: 'DOTA2' },
  { label: 'CS:GO', value: 'CSGO' },
  { label: '王者荣耀', value: 'WZRY' },
  { label: '和平精英', value: 'HPJY' },
  { label: '永劫无间', value: 'YJWJ' },
  { label: '绝地求生', value: 'PUBG' },
  { label: '守望先锋', value: 'OW' }
]

export const RANKS = {
  LOL: [
    { label: '黑铁', value: 'IRON' },
    { label: '青铜', value: 'BRONZE' },
    { label: '白银', value: 'SILVER' },
    { label: '黄金', value: 'GOLD' },
    { label: '铂金', value: 'PLATINUM' },
    { label: '钻石', value: 'DIAMOND' },
    { label: '大师', value: 'MASTER' },
    { label: '宗师', value: 'GRANDMASTER' },
    { label: '王者', value: 'CHALLENGER' }
  ],
  DOTA2: [
    { label: '先锋', value: 'HERALD' },
    { label: '卫士', value: 'GUARDIAN' },
    { label: '十字军', value: 'CRUSADER' },
    { label: '传奇', value: 'LEGEND' },
    { label: '万古流芳', value: 'ANCIENT' },
    { label: '超凡入圣', value: 'DIVINE' },
    { label: '冠绝一世', value: 'IMMORTAL' }
  ],
  DEFAULT: [
    { label: '青铜', value: 'BRONZE' },
    { label: '白银', value: 'SILVER' },
    { label: '黄金', value: 'GOLD' },
    { label: '铂金', value: 'PLATINUM' },
    { label: '钻石', value: 'DIAMOND' }
  ]
}

export const POSITIONS = {
  LOL: [
    { label: '上单', value: 'TOP' },
    { label: '打野', value: 'JUNGLE' },
    { label: '中单', value: 'MID' },
    { label: 'ADC', value: 'ADC' },
    { label: '辅助', value: 'SUPPORT' }
  ],
  DOTA2: [
    { label: '一号位', value: 'POS1' },
    { label: '二号位', value: 'POS2' },
    { label: '三号位', value: 'POS3' },
    { label: '四号位', value: 'POS4' },
    { label: '五号位', value: 'POS5' }
  ],
  DEFAULT: [
    { label: '前锋', value: 'FORWARD' },
    { label: '中场', value: 'MIDDLE' },
    { label: '后卫', value: 'DEFENDER' },
    { label: '自由人', value: 'FREE' }
  ]
}

export const PLAY_STYLES = [
  { label: '激进进攻', value: 'AGGRESSIVE' },
  { label: '稳健防守', value: 'DEFENSIVE' },
  { label: '战术指挥', value: 'STRATEGIC' },
  { label: '辅助支援', value: 'SUPPORTIVE' },
  { label: '全能型', value: 'BALANCED' }
]

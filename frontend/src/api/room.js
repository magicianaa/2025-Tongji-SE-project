import request from '@/utils/request'

/**
 * 获取所有房间列表
 */
export const getRoomList = () => {
  return request({
    url: '/rooms',
    method: 'get'
  })
}

/**
 * 根据状态筛选房间
 */
export const getRoomsByStatus = (status) => {
  return request({
    url: `/rooms/status/${status}`,
    method: 'get'
  })
}

/**
 * 获取房间详情
 */
export const getRoomDetail = (roomId) => {
  return request({
    url: `/rooms/${roomId}`,
    method: 'get'
  })
}

/**
 * 办理入住
 */
export const checkIn = (data) => {
  return request({
    url: '/rooms/check-in',
    method: 'post',
    data
  })
}

/**
 * 办理退房
 */
export const checkOut = (recordId, paymentMethod = 'CASH') => {
  return request({
    url: `/rooms/check-out/${recordId}`,
    method: 'post',
    params: { paymentMethod }
  })
}

/**
 * 获取用户的入住记录
 */
export const getCheckInRecords = () => {
  return request({
    url: '/rooms/check-in-records',
    method: 'get'
  })
}

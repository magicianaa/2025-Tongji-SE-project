import request from '@/utils/request'

/**
 * 获取账单详情（根据入住记录ID）
 */
export function getBillDetail(recordId) {
  return request({
    url: `/billing/detail/${recordId}`,
    method: 'get'
  })
}

/**
 * 获取账单详情（根据房间ID）
 */
export function getBillDetailByRoomId(roomId) {
  return request({
    url: `/billing/detail/room/${roomId}`,
    method: 'get'
  })
}

/**
 * 账单清付
 */
export function settleBill(recordId, paymentMethod) {
  return request({
    url: `/billing/settle/${recordId}`,
    method: 'post',
    params: { paymentMethod }
  })
}

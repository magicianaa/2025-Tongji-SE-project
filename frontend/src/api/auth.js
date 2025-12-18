import request from '@/utils/request'

/**
 * 用户注册
 */
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 发送短信验证码
 */
export const sendSmsCode = (phone) => {
  return request({
    url: '/auth/sms/send',
    method: 'post',
    params: { phone }
  })
}

/**
 * 用户登录
 */
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data: {
      username: data.phone,  // 后端期望username字段
      password: data.password
    }
  })
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => {
  return request({
    url: '/auth/current-user',
    method: 'get'
  })
}

/**
 * 检查入住状态
 */
export const checkInStatus = () => {
  return request({
    url: '/auth/check-in-status',
    method: 'get'
  })
}

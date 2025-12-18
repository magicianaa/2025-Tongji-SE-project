import { defineStore } from 'pinia'
import { ref } from 'vue'
import router from '@/router'
import { checkInStatus } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const roomAuthToken = ref(localStorage.getItem('roomAuthToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const checkInInfo = ref(JSON.parse(localStorage.getItem('checkInInfo') || '{}'))
  
  // Actions
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setRoomAuthToken = (newToken) => {
    roomAuthToken.value = newToken
    localStorage.setItem('roomAuthToken', newToken)
  }
  
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setCheckInInfo = (info) => {
    checkInInfo.value = info
    localStorage.setItem('checkInInfo', JSON.stringify(info))
  }
  
  /**
   * 检查并更新入住状态
   */
  const refreshCheckInStatus = async () => {
    if (!token.value) {
      console.log('⚠️ 未登录，跳过入住状态检查')
      return
    }

    // 延迟一小段时间，确保token已经被设置到axios请求头中
    await new Promise(resolve => setTimeout(resolve, 100))

    try {
      const res = await checkInStatus()
      console.log('📡 入住状态检查响应:', res)
      
      if (res && res.hasCheckIn) {
        // 有有效入住记录，设置roomAuthToken为当前的普通token
        // 因为后端验证时会通过token查询入住记录
        setRoomAuthToken(token.value)
        setCheckInInfo(res)
        console.log('✅ 入住状态有效，已设置客房权限')
        console.log('   - recordId:', res.recordId)
        console.log('   - roomId:', res.roomId)
        console.log('   - guestId:', res.guestId)
      } else {
        // 无有效入住记录，清除roomAuthToken
        setRoomAuthToken('')
        setCheckInInfo({})
        console.log('⚠️ 未入住或入住已过期:', res?.message || '无有效入住记录')
      }
    } catch (error) {
      console.error('❌ 检查入住状态失败:', error)
      setRoomAuthToken('')
      setCheckInInfo({})
    }
  }
  
  const logout = () => {
    token.value = ''
    roomAuthToken.value = ''
    userInfo.value = {}
    checkInInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('roomAuthToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('checkInInfo')
    router.push('/login')
  }
  
  // Getters
  const isLoggedIn = () => {
    return !!token.value
  }
  
  const hasRoomAuth = () => {
    return !!roomAuthToken.value
  }
  
  return {
    token,
    roomAuthToken,
    userInfo,
    checkInInfo,
    setToken,
    setRoomAuthToken,
    setUserInfo,
    setCheckInInfo,
    refreshCheckInStatus,
    logout,
    isLoggedIn,
    hasRoomAuth
  }
})

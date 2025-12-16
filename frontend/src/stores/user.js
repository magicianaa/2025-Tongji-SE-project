import { defineStore } from 'pinia'
import { ref } from 'vue'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const roomAuthToken = ref(localStorage.getItem('roomAuthToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
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
  
  const logout = () => {
    token.value = ''
    roomAuthToken.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('roomAuthToken')
    localStorage.removeItem('userInfo')
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
    setToken,
    setRoomAuthToken,
    setUserInfo,
    logout,
    isLoggedIn,
    hasRoomAuth
  }
})

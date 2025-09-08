import { defineStore } from 'pinia'
import { authAPI } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    menus: [],
    permissions: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    // 检查是否有指定权限
    hasPermission: (state) => (permission) => {
      if (!permission) return true
      return state.permissions.includes(permission)
    }
  },

  actions: {
    async login(loginData) {
      try {
        const response = await authAPI.login(loginData)
        this.token = response.data.token
        localStorage.setItem('token', this.token)
        return response
      } catch (error) {
        throw error
      }
    },

    async getUserInfo() {
      try {
        const response = await authAPI.getUserInfo()
        this.userInfo = response.data
        this.menus = response.data.menus || []
        this.permissions = response.data.permissions || []
        return response
      } catch (error) {
        throw error
      }
    },

    async logout() {
      try {
        await authAPI.logout()
      } catch (error) {
        // 即使API调用失败，也要清除本地数据
        console.error('Logout API failed:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        this.menus = []
        this.permissions = []
        localStorage.removeItem('token')
      }
    },

    clearUserData() {
      this.token = ''
      this.userInfo = null
      this.menus = []
      this.permissions = []
      localStorage.removeItem('token')
    }
  }
})
import request from '@/utils/request'

export const authAPI = {
  // 登录
  login(data) {
    return request.post('/auth/login', data)
  },

  // 获取用户信息
  getUserInfo() {
    return request.get('/auth/userinfo')
  },

  // 退出登录
  logout() {
    return request.post('/auth/logout')
  },

  // 修改密码
  changePassword(data) {
    return request.post('/auth/changePassword', data)
  }
}
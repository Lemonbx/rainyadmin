import request from '@/utils/request'

export const userAPI = {
  // 获取用户列表
  getUserList(params) {
    return request.get('/user', { params })
  },

  // 根据ID获取用户
  getUserById(id) {
    return request.get(`/user/${id}`)
  },

  // 创建用户
  createUser(data) {
    return request.post('/user', data)
  },

  // 更新用户
  updateUser(data) {
    return request.put('/user', data)
  },

  // 删除用户
  deleteUser(id) {
    return request.delete(`/user/${id}`)
  }
}
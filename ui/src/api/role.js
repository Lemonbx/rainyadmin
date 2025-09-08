import request from '@/utils/request'

export const roleAPI = {
  // 获取角色列表
  getRoleList(params) {
    return request.get('/role', { params })
  },

  // 根据ID获取角色
  getRoleById(id) {
    return request.get(`/role/${id}`)
  },

  // 创建角色
  createRole(data) {
    return request.post('/role', data)
  },

  // 更新角色
  updateRole(data) {
    return request.put('/role', data)
  },

  // 删除角色
  deleteRole(id) {
    return request.delete(`/role/${id}`)
  },

  // 获取所有角色
  getAllRoles() {
    return request.get('/role/all')
  }
}
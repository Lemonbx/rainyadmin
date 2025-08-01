import request from '@/utils/request'

export const menuAPI = {
  // 获取菜单列表
  getMenuList(params) {
    return request.get('/menu', { params })
  },

  // 根据ID获取菜单
  getMenuById(id) {
    return request.get(`/menu/${id}`)
  },

  // 创建菜单
  createMenu(data) {
    return request.post('/menu', data)
  },

  // 更新菜单
  updateMenu(data) {
    return request.put('/menu', data)
  },

  // 删除菜单
  deleteMenu(id) {
    return request.delete(`/menu/${id}`)
  },

  // 获取菜单树
  getMenuTree() {
    return request.get('/menu/tree')
  },

  // 获取所有菜单
  getAllMenus() {
    return request.get('/menu/all')
  }
}
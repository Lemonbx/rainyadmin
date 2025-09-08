import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 基础路由（不需要权限的路由）
const basicRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requireAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

// 主界面路由
const mainRoute = {
  path: '/',
  name: 'MainLayout',
  component: () => import('@/layout/MainLayout.vue'),
  meta: { requireAuth: true },
  children: [
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { title: '首页', keepAlive: true }
    }
  ]
}



const router = createRouter({
  history: createWebHistory('/admin/'),
  routes: [...basicRoutes, mainRoute]
})

// 动态添加路由 - 完全从接口数据生成
export function addDynamicRoutes(userMenus, userPermissions) {
  // 先清除已存在的动态路由，避免重复添加
  const userStore = useUserStore()
  if (userStore.dynamicRoutesAdded) {
    return
  }
  
  // 递归构建路由
  function buildRoutes(menus) {
    const routes = []
    
    menus.forEach(menu => {
      // 只处理页面类型的菜单 (type === 1)
      if (menu.type === 1 && menu.path && menu.component) {
        const route = {
          path: menu.path,
          name: menu.component,
          component: () => import(`@/views/${menu.component}.vue`),
          meta: {
            title: menu.name,
            keepAlive: true,
            menuId: menu.id
          }
        }
        routes.push(route)
      }
      
      // 递归处理子菜单
      if (menu.children && menu.children.length > 0) {
        const childRoutes = buildRoutes(menu.children)
        routes.push(...childRoutes)
      }
    })
    
    return routes
  }
  
  // 从菜单数据构建路由
  const dynamicRoutes = buildRoutes(userMenus)
  
  // 添加到主路由的children中
  dynamicRoutes.forEach(route => {
    router.addRoute('MainLayout', route)
  })
  
  // 标记动态路由已添加
  userStore.dynamicRoutesAdded = true
  
  console.log('动态路由已添加:', dynamicRoutes)
}





// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 不需要认证的路由直接通过
  if (to.meta?.requireAuth === false) {
    if (to.path === '/login' && userStore.isLoggedIn) {
      // 已登录用户访问登录页，重定向到首页
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  // 需要认证的路由
  if (!userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 已登录但没有用户信息或动态路由未添加
  if (!userStore.userInfo || !userStore.dynamicRoutesAdded) {
    try {
      // 获取用户信息（如果还没有）
      if (!userStore.userInfo) {
        await userStore.getUserInfo()
      }
      
      // 添加动态路由（如果还没有添加）
      if (!userStore.dynamicRoutesAdded) {
        addDynamicRoutes(userStore.userInfo.menus, userStore.userInfo.permissions)
      }
      
      // 重新导航到目标路由
      next({ ...to, replace: true })
    } catch (error) {
      console.error('获取用户信息失败:', error)
      userStore.clearUserData()
      next('/login')
    }
    return
  }

  next()
})

export default router

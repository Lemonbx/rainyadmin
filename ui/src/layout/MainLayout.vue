<template>
  <n-layout has-sider style="height: 100vh">
    <!-- 侧边栏 -->
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="logo">
        <h3 v-if="!collapsed">管理系统</h3>
        <span v-else>MS</span>
      </div>
      
      <n-menu
        v-model:value="activeMenuKey"
        v-model:expanded-keys="expandedKeys"
        :collapsed="collapsed"
        :options="menuOptions"
        @update:value="handleMenuSelect"
        @update:expanded-keys="handleExpandedKeysChange"
      />
    </n-layout-sider>

    <!-- 主内容区 -->
    <n-layout>
      <!-- 顶部栏 -->
      <n-layout-header bordered style="height: 60px; padding: 0 16px">
        <div class="header-content">
          <div class="header-left">
            <n-breadcrumb>
              <n-breadcrumb-item>首页</n-breadcrumb-item>
              <n-breadcrumb-item v-if="currentBreadcrumb">{{ currentBreadcrumb }}</n-breadcrumb-item>
            </n-breadcrumb>
          </div>
          
          <div class="header-right">
            <n-dropdown :options="userDropdownOptions" @select="handleUserDropdown">
              <n-button text>
                <n-icon :size="16">
                  <PersonOutline />
                </n-icon>
                {{ userStore.userInfo?.nickname || '用户' }}
              </n-button>
            </n-dropdown>
          </div>
        </div>
      </n-layout-header>

      <!-- Tab栏 -->
      <div class="tabs-container">
        <n-tabs
          v-model:value="tabsStore.activeTab"
          type="card"
          closable
          @close="handleTabClose"
          @update:value="handleTabChange"
        >
          <n-tab-pane
            v-for="tab in tabsStore.tabs"
            :key="tab.name"
            :name="tab.name"
            :tab="tab.label"
            :closable="tab.closable"
          />
        </n-tabs>
      </div>

      <!-- 页面内容 -->
      <n-layout-content style="padding: 16px">
        <router-view v-slot="{ Component, route }">
          <keep-alive :include="tabsStore.cachedViews">
            <component :is="Component" :key="route.name" />
          </keep-alive>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>

  <!-- 修改密码弹窗 -->
  <ChangePasswordModal v-model:show="showChangePasswordModal" />
</template>

<script setup>
import { ref, computed, onMounted, h, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useTabsStore } from '@/stores/tabs'
import { useMessage } from 'naive-ui'
import {
  PersonOutline,
  HomeOutline,
  PeopleOutline,
  ShieldOutline,
  MenuOutline,
  LogOutOutline,
  KeyOutline
} from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const tabsStore = useTabsStore()
const message = useMessage()

const collapsed = ref(false)
const activeMenuKey = ref('dashboard')
const expandedKeys = ref([])

// 渲染图标
const renderIcon = (icon) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 菜单选项 - 从用户权限菜单动态生成
const menuOptions = computed(() => {
  if (!userStore.userInfo?.menus) {
    return [
      {
        label: '首页',
        key: 'dashboard',
        icon: renderIcon(HomeOutline)
      }
    ]
  }
  
  // 递归构建菜单树，过滤掉按钮类型的菜单
  function buildMenuOptions(menus) {
    return menus
      .filter(menu => menu.type !== 2) // 过滤掉按钮类型的菜单（type=2）
      .map(menu => {
        const option = {
          label: menu.name,
          key: menu.component || menu.id.toString(),
          icon: renderIcon(getMenuIcon(menu.logo))
        }
        
        // 如果有子菜单，递归构建（也要过滤）
        if (menu.children && menu.children.length > 0) {
          const filteredChildren = buildMenuOptions(menu.children)
          if (filteredChildren.length > 0) {
            option.children = filteredChildren
          }
        }
        
        return option
      })
  }
  
  // 添加固定的首页菜单
  const dynamicMenus = buildMenuOptions(userStore.userInfo.menus)
  return [
    {
      label: '首页',
      key: 'dashboard',
      icon: renderIcon(HomeOutline)
    },
    ...dynamicMenus
  ]
})

// 根据图标名获取对应的图标组件
const getMenuIcon = (iconName) => {
  const iconMap = {
    'Person': PeopleOutline,
    'Shield': ShieldOutline,
    'Menu': MenuOutline,
    'Settings': MenuOutline
  }
  return iconMap[iconName] || MenuOutline
}

// 用户下拉菜单
const userDropdownOptions = [
  {
    label: '修改密码',
    key: 'changePassword',
    icon: renderIcon(KeyOutline)
  },
  {
    type: 'divider'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline)
  }
]

// 当前面包屑
const currentBreadcrumb = computed(() => {
  if (activeMenuKey.value === 'dashboard') {
    return ''
  }
  
  // 从用户菜单中查找当前菜单项
  function findMenuByKey(menus, targetKey) {
    for (const menu of menus) {
      if ((menu.component || menu.id.toString()) === targetKey) {
        return menu
      }
      if (menu.children) {
        const found = findMenuByKey(menu.children, targetKey)
        if (found) return found
      }
    }
    return null
  }
  
  const menuItem = findMenuByKey(userStore.userInfo?.menus || [], activeMenuKey.value)
  return menuItem?.name || ''
})

// 处理菜单选择
const handleMenuSelect = (key) => {
  activeMenuKey.value = key
  
  // 如果是首页
  if (key === 'dashboard') {
    tabsStore.addTab({
      name: 'dashboard',
      label: '首页',
      path: '/dashboard',
      keepAlive: true
    })
    router.push('/dashboard')
    return
  }
  
  // 从用户菜单中查找对应的菜单项
  function findMenuByKey(menus, targetKey) {
    for (const menu of menus) {
      if ((menu.component || menu.id.toString()) === targetKey) {
        return menu
      }
      if (menu.children) {
        const found = findMenuByKey(menu.children, targetKey)
        if (found) return found
      }
    }
    return null
  }
  
  const menuItem = findMenuByKey(userStore.userInfo?.menus || [], key)
  if (menuItem && menuItem.type === 1 && menuItem.path) {
    tabsStore.addTab({
      name: menuItem.component || key, // 直接使用接口返回的组件名
      label: menuItem.name,
      path: menuItem.path,
      keepAlive: true
    })
    router.push(menuItem.path)
  }
}

// 处理Tab关闭
const handleTabClose = (tabName) => {
  tabsStore.removeTab(tabName)
  
  // 如果关闭的是当前tab，跳转到新的活动tab
  if (tabsStore.activeTab) {
    const activeTab = tabsStore.tabs.find(tab => tab.name === tabsStore.activeTab)
    if (activeTab) {
      router.push(activeTab.path)
      activeMenuKey.value = tabsStore.activeTab
    }
  }
}

// 处理Tab切换
const handleTabChange = (tabName) => {
  const tab = tabsStore.tabs.find(t => t.name === tabName)
  if (tab) {
    tabsStore.setActiveTab(tabName)
    router.push(tab.path)
    activeMenuKey.value = tabName
  }
}

// 修改密码弹窗状态
const showChangePasswordModal = ref(false)

// 处理用户下拉菜单
const handleUserDropdown = async (key) => {
  if (key === 'changePassword') {
    showChangePasswordModal.value = true
  } else if (key === 'logout') {
    try {
      await userStore.logout()
      tabsStore.clearAllTabs()
      router.push('/login')
      message.success('退出成功')
    } catch (error) {
      message.error('退出失败')
    }
  }
}

// 恢复当前页面的tab
const restoreCurrentTab = () => {
  const currentPath = route.path
  
  // 如果不是首页且没有对应的tab，则添加
  if (currentPath !== '/dashboard' && currentPath !== '/') {
    const existingTab = tabsStore.tabs.find(t => t.path === currentPath)
    if (!existingTab && userStore.userInfo?.menus) {
      // 从用户菜单中查找对应的菜单项
      const menuItem = findMenuByPath(userStore.userInfo.menus, currentPath)
      if (menuItem) {
        tabsStore.addTab({
          name: menuItem.component || menuItem.id.toString(), // 直接使用接口返回的组件名
          label: menuItem.name,
          path: menuItem.path,
          keepAlive: true
        })
      }
    } else if (existingTab) {
      // 如果tab已存在，设置为活动tab
      tabsStore.setActiveTab(existingTab.name)
    }
  }
}

// 递归查找菜单项
const findMenuByPath = (menus, targetPath) => {
  if (!menus) return null
  
  for (const menu of menus) {
    if (menu.path === targetPath && menu.type === 1) { // 只查找页面类型的菜单
      return menu
    }
    if (menu.children) {
      const found = findMenuByPath(menu.children, targetPath)
      if (found) return found
    }
  }
  return null
}

// 根据路径设置活动菜单和展开状态
const setActiveMenuFromPath = () => {
  const currentPath = route.path
  
  if (currentPath === '/dashboard' || currentPath === '/') {
    activeMenuKey.value = 'dashboard'
    expandedKeys.value = []
    return
  }
  
  // 从用户菜单中查找对应的菜单项
  if (userStore.userInfo?.menus) {
    const menuInfo = findMenuAndParentByPath(userStore.userInfo.menus, currentPath)
    if (menuInfo) {
      activeMenuKey.value = menuInfo.menu.component || menuInfo.menu.id.toString()
      
      // 如果有父级菜单，确保展开
      if (menuInfo.parentKeys.length > 0) {
        expandedKeys.value = [...menuInfo.parentKeys]
      }
    }
  }
}

// 递归查找菜单项及其父级路径
const findMenuAndParentByPath = (menus, targetPath, parentKeys = []) => {
  for (const menu of menus) {
    if (menu.path === targetPath && menu.type === 1) {
      return {
        menu: menu,
        parentKeys: [...parentKeys]
      }
    }
    
    if (menu.children && menu.children.length > 0) {
      const menuKey = menu.component || menu.id.toString()
      const result = findMenuAndParentByPath(
        menu.children, 
        targetPath, 
        [...parentKeys, menuKey]
      )
      if (result) {
        return result
      }
    }
  }
  return null
}

// 处理菜单展开变化
const handleExpandedKeysChange = (keys) => {
  expandedKeys.value = keys
}

// 监听路由变化，更新菜单状态
watch(
  () => route.path,
  () => {
    setActiveMenuFromPath()
  }
)

onMounted(() => {
  // 设置活动菜单和展开状态
  setActiveMenuFromPath()
  
  // 恢复当前页面的tab
  restoreCurrentTab()
})
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e0e0e6;
  margin-bottom: 8px;
}

.logo h3 {
  margin: 0;
  color: #18a058;
  font-weight: bold;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tabs-container {
  padding: 0 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #e0e0e6;
}

:deep(.n-tabs .n-tabs-nav) {
  background-color: transparent;
}
</style>
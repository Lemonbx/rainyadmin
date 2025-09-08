import { defineStore } from 'pinia'

export const useTabsStore = defineStore('tabs', {
  state: () => ({
    tabs: [
      {
        name: 'dashboard',
        label: '首页',
        path: '/dashboard',
        closable: false
      }
    ],
    activeTab: 'dashboard',
    cachedViews: ['Dashboard'] // 存储需要缓存的组件名称
  }),

  actions: {
    addTab(tab) {
      const existingTab = this.tabs.find(t => t.name === tab.name)
      if (!existingTab) {
        this.tabs.push({
          ...tab,
          closable: true
        })
      }
      this.activeTab = tab.name
      
      // 如果是支持缓存的tab，添加到缓存列表
      if (tab.keepAlive !== false) {
        // 确保使用正确的组件名称进行缓存
        this.addCachedView(tab.name)
      }
    },

    removeTab(tabName) {
      const index = this.tabs.findIndex(t => t.name === tabName)
      if (index > -1 && this.tabs[index].closable) {
        this.tabs.splice(index, 1)
        
        // 移除缓存
        this.removeCachedView(tabName)
        
        // 如果关闭的是当前活动tab，切换到其他tab
        if (this.activeTab === tabName) {
          if (this.tabs.length > 0) {
            this.activeTab = this.tabs[Math.max(0, index - 1)].name
          }
        }
      }
    },

    setActiveTab(tabName) {
      this.activeTab = tabName
    },

    clearAllTabs() {
      this.tabs = [
        {
          name: 'dashboard',
          label: '首页',
          path: '/dashboard',
          closable: false
        }
      ]
      this.activeTab = 'dashboard'
      this.cachedViews = ['Dashboard']
    },

    // 缓存管理方法
    addCachedView(viewName) {
      if (!this.cachedViews.includes(viewName)) {
        this.cachedViews.push(viewName)
      }
    },

    removeCachedView(viewName) {
      const index = this.cachedViews.indexOf(viewName)
      if (index > -1) {
        this.cachedViews.splice(index, 1)
      }
    },

    clearCachedViews() {
      this.cachedViews = ['Dashboard']
    }
  }
})
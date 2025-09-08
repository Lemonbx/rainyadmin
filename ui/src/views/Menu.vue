<template>
  <div class="menu-management">
    <!-- 搜索和操作栏 -->
    <n-card>
      <n-space vertical>
        <n-form
          ref="searchFormRef"
          :model="searchForm"
          label-placement="left"
          label-width="auto"
          inline
        >
          <n-form-item label="菜单名称">
            <n-input
              v-model:value="searchForm.name"
              placeholder="请输入菜单名称"
              clearable
            />
          </n-form-item>
          <n-form-item label="菜单类型">
            <n-select
              v-model:value="searchForm.type"
              :options="typeOptions"
              placeholder="请选择菜单类型"
              clearable
            />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">搜索</n-button>
              <n-button @click="handleReset">重置</n-button>
              <n-button type="primary" @click="handleAdd">新增菜单</n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </n-space>
    </n-card>

    <!-- 数据表格 -->
    <n-card style="margin-top: 16px">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        default-expand-all
        :cascade="false"
        children-key="children"
      />
    </n-card>

    <!-- 新增/编辑菜单弹窗 -->
    <n-modal
      v-model:show="showModal"
      preset="dialog"
      :title="modalTitle"
      style="width: 600px"
    >
      <n-form
        ref="menuFormRef"
        :model="menuForm"
        :rules="menuRules"
        label-placement="left"
        label-width="100px"
      >
        <n-form-item label="菜单名称" path="name">
          <n-input v-model:value="menuForm.name" placeholder="请输入菜单名称" />
        </n-form-item>
        <n-form-item label="菜单类型" path="type">
          <n-select
            v-model:value="menuForm.type"
            :options="typeOptions"
            placeholder="请选择菜单类型"
          />
        </n-form-item>
        <!-- 新增：排序 -->
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="menuForm.sort" :min="0" />
        </n-form-item>
        <n-form-item label="父级菜单">
          <n-tree-select
            v-model:value="menuForm.parentId"
            :options="parentMenuOptions"
            placeholder="请选择父级菜单"
            clearable
            key-field="id"
            label-field="name"
            children-field="children"
          />
        </n-form-item>
        <n-form-item label="菜单图标">
          <n-input v-model:value="menuForm.logo" placeholder="请输入菜单图标" />
        </n-form-item>
        <n-form-item v-if="menuForm.type === 1" label="路由路径" path="path">
          <n-input v-model:value="menuForm.path" placeholder="请输入路由路径，如：/user" />
        </n-form-item>
        <n-form-item v-if="menuForm.type === 1" label="组件路径">
          <n-input v-model:value="menuForm.component" placeholder="请输入组件路径，如：User" />
        </n-form-item>
        <n-form-item v-if="menuForm.type === 2" label="权限标识" path="perms">
          <n-input v-model:value="menuForm.perms" placeholder="请输入权限标识，如：user:create" />
        </n-form-item>
      </n-form>
      
      <template #action>
        <n-space>
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script>
export default {
  name: 'Menu'
}
</script>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { menuAPI } from '@/api/menu'
import { NButton, NTag, NSpace, NIcon } from 'naive-ui'
import { FolderOutline, DocumentOutline, KeyOutline } from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const parentMenuOptions = ref([])

const typeOptions = [
  { label: '目录', value: 0 },
  { label: '页面', value: 1 },
  { label: '按钮', value: 2 }
]

const searchForm = reactive({
  name: '',
  type: null
})

const menuForm = reactive({
  id: null,
  name: '',
  logo: '',
  path: '',
  perms: '',
  type: 0,
  component: '',
  parentId: null,
  // 新增：排序
  sort: 0
})

const menuRules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: ['input', 'blur'] }
  ],
  type: [
    { required: true, type: 'number', message: '请选择菜单类型', trigger: ['change'] }
  ],
  path: [
    { required: true, message: '请输入路由路径', trigger: ['input', 'blur'] }
  ],
  perms: [
    { required: true, message: '请输入权限标识', trigger: ['input', 'blur'] }
  ]
}

const modalTitle = computed(() => {
  return menuForm.id ? '编辑菜单' : '新增菜单'
})

// 表格列定义
const columns = [
  {
    title: '菜单名称',
    key: 'name',
    render: (row) => {
      return h('span', { style: { fontWeight: 'bold' } }, row.name)
    }
  },
  {
    title: '图标',
    key: 'logo',
    width: 80,
    render: (row) => {
      return row.logo ? h('span', row.logo) : '-'
    }
  },
  {
    title: '类型',
    key: 'type',
    width: 100,
    render: (row) => {
      const typeMap = {
        0: { label: '目录', type: 'info', icon: FolderOutline },
        1: { label: '页面', type: 'success', icon: DocumentOutline },
        2: { label: '按钮', type: 'warning', icon: KeyOutline }
      }
      const config = typeMap[row.type] || {}
      return h(NTag, { type: config.type }, {
        icon: () => h(NIcon, { component: config.icon }),
        default: () => config.label
      })
    }
  },
  {
    title: '路由路径',
    key: 'path',
    render: (row) => row.path || '-'
  },
  {
    title: '权限标识',
    key: 'perms',
    render: (row) => row.perms || '-'
  },
  {
    title: '组件路径',
    key: 'component',
    render: (row) => row.component || '-'
  },
  // 新增：排序列
  {
    title: '排序',
    key: 'sort',
    width: 80,
    render: (row) => row.sort ?? 0
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleAddChild(row)
          }, { default: () => '新增子菜单' }),
          h(NButton, {
            size: 'small',
            type: 'info',
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row)
          }, { default: () => '删除' })
        ]
      })
    }
  }
]



// 获取菜单列表
const getMenuList = async () => {
  try {
    loading.value = true
    const response = await menuAPI.getMenuList(searchForm)
    const allMenus = response.data || []
    tableData.value = allMenus
  } catch (error) {
    message.error(error.message || '获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取父级菜单选项
const getParentMenuOptions = async () => {
  try {
    const response = await menuAPI.getMenuTree()
    parentMenuOptions.value = response.data || []
  } catch (error) {
    message.error('获取父级菜单失败')
  }
}

// 搜索
const handleSearch = () => {
  getMenuList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    type: null
  })
  handleSearch()
}

// 新增菜单
const handleAdd = () => {
  Object.assign(menuForm, {
    id: null,
    name: '',
    logo: '',
    path: '',
    perms: '',
    type: 0,
    component: '',
    parentId: null,
    // 新增：默认排序
    sort: 0
  })
  showModal.value = true
}

// 新增子菜单
const handleAddChild = (parent) => {
  Object.assign(menuForm, {
    id: null,
    name: '',
    logo: '',
    path: '',
    perms: '',
    type: parent.type === 0 ? 1 : 2,
    component: '',
    parentId: parent.id,
    // 新增：默认排序
    sort: 0
  })
  showModal.value = true
}

// 编辑菜单
const handleEdit = async (row) => {
  try {
    const response = await menuAPI.getMenuById(row.id)
    const menu = response.data
    Object.assign(menuForm, {
      id: menu.id,
      name: menu.name,
      logo: menu.logo,
      path: menu.path,
      perms: menu.perms,
      type: menu.type,
      component: menu.component,
      parentId: menu.parentId,
      // 新增：排序
      sort: menu.sort ?? 0
    })
    showModal.value = true
  } catch (error) {
    message.error('获取菜单信息失败')
  }
}

// 删除菜单
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除菜单"${row.name}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await menuAPI.deleteMenu(row.id)
        message.success('删除成功')
        getMenuList()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await menuFormRef.value?.validate()
    submitLoading.value = true
    
    if (menuForm.id) {
      // 编辑
      await menuAPI.updateMenu(menuForm)
      message.success('更新成功')
    } else {
      // 新增
      await menuAPI.createMenu(menuForm)
      message.success('创建成功')
    }
    
    showModal.value = false
    getMenuList()
    getParentMenuOptions()
  } catch (error) {
    if (error.message) {
      message.error(error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getMenuList()
  getParentMenuOptions()
})
</script>

<style scoped>
.menu-management {
  padding: 16px;
}
</style>
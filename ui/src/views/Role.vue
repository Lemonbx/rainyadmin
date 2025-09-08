<template>
  <div class="role-management">
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
          <n-form-item label="角色名称">
            <n-input
              v-model:value="searchForm.name"
              placeholder="请输入角色名称"
              clearable
            />
          </n-form-item>
          <n-form-item label="角色标识">
            <n-input
              v-model:value="searchForm.key"
              placeholder="请输入角色标识"
              clearable
            />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">搜索</n-button>
              <n-button @click="handleReset">重置</n-button>
              <n-button 
                v-if="userStore.hasPermission('role:create')"
                type="primary" 
                @click="handleAdd"
              >
                新增角色
              </n-button>
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
        :pagination="pagination"
        :remote="true"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 新增/编辑角色弹窗 -->
    <n-modal
      v-model:show="showModal"
      preset="dialog"
      :title="modalTitle"
      style="width: 800px"
    >
      <n-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-placement="left"
        label-width="80px"
      >
        <n-form-item label="角色名称" path="name">
          <n-input v-model:value="roleForm.name" placeholder="请输入角色名称" />
        </n-form-item>
        <n-form-item label="角色标识" path="key">
          <n-input
            v-model:value="roleForm.key"
            placeholder="请输入角色标识"
            :disabled="!!roleForm.id"
          />
        </n-form-item>
        <n-form-item label="权限菜单" path="menuIds">
          <n-tree
            v-model:checked-keys="roleForm.menuIds"
            :data="menuTreeData"
            checkable
            key-field="id"
            label-field="name"
            children-field="children"
            style="max-height: 300px; overflow-y: auto;"
          />
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
  name: 'Role'
}
</script>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { roleAPI } from '@/api/role'
import { menuAPI } from '@/api/menu'
import { NButton, NTag, NSpace } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const menuTreeData = ref([])
const roleFormRef = ref(null)

const searchForm = reactive({
  name: '',
  key: ''
})

const roleForm = reactive({
  id: null,
  name: '',
  key: '',
  menuIds: []
})

const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: ['input', 'blur'] }
  ],
  key: [
    { required: true, message: '请输入角色标识', trigger: ['input', 'blur'] }
  ]
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageCount: 0,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const modalTitle = computed(() => {
  return roleForm.id ? '编辑角色' : '新增角色'
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '角色名称',
    key: 'name'
  },
  {
    title: '角色标识',
    key: 'key'
  },
  {
    title: '权限菜单',
    key: 'menus',
    render: (row) => {
      return h(NSpace, {}, {
        default: () => row.menus?.slice(0, 3).map(menu => 
          h(NTag, { type: 'info', size: 'small' }, { default: () => menu.name })
        ).concat(
          row.menus?.length > 3 ? [h(NTag, { type: 'warning', size: 'small' }, { default: () => `+${row.menus.length - 3}` })] : []
        ) || []
      })
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    render: (row) => {
      return row.createTime ? new Date(row.createTime).toLocaleString() : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => {
          const buttons = []
          
          // 编辑按钮 - 检查权限
          if (userStore.hasPermission('role:update')) {
            buttons.push(
              h(NButton, {
                size: 'small',
                type: 'primary',
                onClick: () => handleEdit(row)
              }, { default: () => '编辑' })
            )
          }
          
          // 删除按钮 - 检查权限
          if (userStore.hasPermission('role:delete')) {
            buttons.push(
              h(NButton, {
                size: 'small',
                type: 'error',
                onClick: () => handleDelete(row)
              }, { default: () => '删除' })
            )
          }
          
          return buttons
        }
      })
    }
  }
]

// 获取角色列表
const getRoleList = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    }
    
    const response = await roleAPI.getRoleList(params)
    tableData.value = response.data.rows || []
    pagination.itemCount = response.data.totalRowCount || 0
    pagination.pageCount = response.data.totalPageCount || 0
  } catch (error) {
    message.error(error.message || '获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 获取菜单树
const getMenuTree = async () => {
  try {
    const response = await menuAPI.getMenuTree()
    menuTreeData.value = response.data || []
  } catch (error) {
    message.error('获取菜单树失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getRoleList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    key: ''
  })
  handleSearch()
}

// 新增角色
const handleAdd = () => {
  Object.assign(roleForm, {
    id: null,
    name: '',
    key: '',
    menuIds: []
  })
  showModal.value = true
}

// 编辑角色
const handleEdit = async (row) => {
  try {
    const response = await roleAPI.getRoleById(row.id)
    const role = response.data
    Object.assign(roleForm, {
      id: role.id,
      name: role.name,
      key: role.key,
      menuIds: role.menus?.map(menu => menu.id) || []
    })
    showModal.value = true
  } catch (error) {
    message.error('获取角色信息失败')
  }
}

// 删除角色
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除角色"${row.name}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await roleAPI.deleteRole(row.id)
        message.success('删除成功')
        getRoleList()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await roleFormRef.value?.validate()
    submitLoading.value = true
    
    if (roleForm.id) {
      // 编辑
      await roleAPI.updateRole(roleForm)
      message.success('更新成功')
    } else {
      // 新增
      await roleAPI.createRole(roleForm)
      message.success('创建成功')
    }
    
    showModal.value = false
    getRoleList()
  } catch (error) {
    if (error.message) {
      message.error(error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  getRoleList()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  getRoleList()
}

onMounted(() => {
  getRoleList()
  getMenuTree()
})
</script>

<style scoped>
.role-management {
  padding: 16px;
}
</style>
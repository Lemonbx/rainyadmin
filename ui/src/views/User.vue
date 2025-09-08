<template>
  <div class="user-management">
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
          <n-form-item label="用户名">
            <n-input
              v-model:value="searchForm.loginname"
              placeholder="请输入用户名"
              clearable
            />
          </n-form-item>
          <n-form-item label="昵称">
            <n-input
              v-model:value="searchForm.nickname"
              placeholder="请输入昵称"
              clearable
            />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">搜索</n-button>
              <n-button @click="handleReset">重置</n-button>
              <n-button 
                v-if="userStore.hasPermission('user:create')"
                type="primary" 
                @click="handleAdd"
              >
                新增用户
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

    <!-- 新增/编辑用户弹窗 -->
    <n-modal
      v-model:show="showModal"
      preset="dialog"
      :title="modalTitle"
      style="width: 600px"
    >
      <n-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-placement="left"
        label-width="80px"
      >
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="userForm.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="用户名" path="loginname">
          <n-input
            v-model:value="userForm.loginname"
            placeholder="请输入用户名"
            :disabled="!!userForm.id"
          />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="userForm.password"
            type="password"
            show-password-on="mousedown"
            :placeholder="userForm.id ? '不填写则不修改密码' : '请输入密码'"
          />
        </n-form-item>
        <n-form-item label="角色" path="roleIds">
          <n-select
            v-model:value="userForm.roleIds"
            multiple
            :options="roleOptions"
            placeholder="请选择角色"
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
  name: 'User'
}
</script>

<script setup>
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { userAPI } from '@/api/user'
import { roleAPI } from '@/api/role'
import { NButton, NTag, NSpace } from 'naive-ui'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const userFormRef = ref(null)
const loading = ref(false)
const showModal = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const roleOptions = ref([])


const searchForm = reactive({
  loginname: '',
  nickname: ''
})

const userForm = reactive({
  id: null,
  nickname: '',
  loginname: '',
  password: '',
  roleIds: []
})

const userRules = computed(() => ({
  nickname: [
    { required: true, message: '请输入昵称', trigger: ['input', 'blur'] }
  ],
  loginname: [
    { required: true, message: '请输入用户名', trigger: ['input', 'blur'] }
  ],
  password: [
    {
      required: !userForm.id, // 编辑用户时密码不必填
      message: '请输入密码',
      trigger: ['input', 'blur']
    }
  ]
}))

const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageCount: 0,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const modalTitle = computed(() => {
  return userForm.id ? '编辑用户' : '新增用户'
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '昵称',
    key: 'nickname'
  },
  {
    title: '用户名',
    key: 'loginname'
  },
  {
    title: '角色',
    key: 'roles',
    render: (row) => {
      return h(NSpace, {}, {
        default: () => row.roles?.map(role => 
          h(NTag, { type: 'info' }, { default: () => role.name })
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
          if (userStore.hasPermission('user:update')) {
            buttons.push(
              h(NButton, {
                size: 'small',
                type: 'primary',
                onClick: () => handleEdit(row)
              }, { default: () => '编辑' })
            )
          }
          
          // 删除按钮 - 检查权限
          if (userStore.hasPermission('user:delete')) {
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

// 获取用户列表
const getUserList = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    }
    
    const response = await userAPI.getUserList(params)
    tableData.value = response.data.rows || []
    pagination.itemCount = response.data.totalRowCount || 0
    pagination.pageCount = response.data.totalPageCount || 0
  } catch (error) {
    message.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色选项
const getRoleOptions = async () => {
  try {
    const response = await roleAPI.getAllRoles()
    roleOptions.value = (response.data || []).map(role => ({
      label: role.name,
      value: role.id
    }))
  } catch (error) {
    message.error('获取角色列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    loginname: '',
    nickname: ''
  })
  handleSearch()
}

// 新增用户
const handleAdd = () => {
  Object.assign(userForm, {
    id: null,
    nickname: '',
    loginname: '',
    password: '',
    roleIds: []
  })
  showModal.value = true
}

// 编辑用户
const handleEdit = async (row) => {
  try {
    const response = await userAPI.getUserById(row.id)
    const user = response.data
    Object.assign(userForm, {
      id: user.id,
      nickname: user.nickname,
      loginname: user.loginname,
      password: '',
      roleIds: user.roles?.map(role => role.id) || []
    })
    showModal.value = true
  } catch (error) {
    message.error('获取用户信息失败')
  }
}

// 删除用户
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除用户"${row.nickname}"吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await userAPI.deleteUser(row.id)
        message.success('删除成功')
        getUserList()
      } catch (error) {
        message.error(error.message || '删除失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await userFormRef.value?.validate()
    submitLoading.value = true
    
    if (userForm.id) {
      // 编辑
      await userAPI.updateUser(userForm)
      message.success('更新成功')
    } else {
      // 新增
      await userAPI.createUser(userForm)
      message.success('创建成功')
    }
    
    showModal.value = false
    getUserList()
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
  getUserList()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  getUserList()
}

onMounted(() => {
  getUserList()
  getRoleOptions()
})
</script>

<style scoped>
.user-management {
  padding: 16px;
}
</style>
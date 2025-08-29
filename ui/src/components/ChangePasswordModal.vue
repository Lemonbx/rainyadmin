<template>
  <n-modal
    v-model:show="showModal"
    preset="dialog"
    title="修改密码"
    :mask-closable="false"
    style="width: 400px"
  >
    <n-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-placement="left"
      label-width="auto"
      require-mark-placement="right-hanging"
    >
      <n-form-item label="当前密码" path="oldPassword">
        <n-input
          v-model:value="formData.oldPassword"
          type="password"
          show-password-on="mousedown"
          placeholder="请输入当前密码"
        />
      </n-form-item>
      <n-form-item label="新密码" path="newPassword">
        <n-input
          v-model:value="formData.newPassword"
          type="password"
          show-password-on="mousedown"
          placeholder="请输入新密码"
        />
      </n-form-item>
      <n-form-item label="确认密码" path="confirmPassword">
        <n-input
          v-model:value="formData.confirmPassword"
          type="password"
          show-password-on="mousedown"
          placeholder="请再次输入新密码"
        />
      </n-form-item>
    </n-form>
    
    <template #action>
      <n-space>
        <n-button @click="handleCancel">取消</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { authAPI } from '@/api/auth'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:show'])

const message = useMessage()
const formRef = ref()
const loading = ref(false)

const showModal = ref(false)

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: [
    {
      required: true,
      message: '请输入当前密码',
      trigger: ['input', 'blur']
    }
  ],
  newPassword: [
    {
      required: true,
      message: '请输入新密码',
      trigger: ['input', 'blur']
    },
    {
      min: 6,
      message: '密码长度不能少于6位',
      trigger: ['input', 'blur']
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: '请确认新密码',
      trigger: ['input', 'blur']
    },
    {
      validator: (rule, value) => {
        if (value !== formData.newPassword) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: ['input', 'blur']
    }
  ]
}

// 监听props变化
watch(
  () => props.show,
  (newVal) => {
    showModal.value = newVal
    if (newVal) {
      // 重置表单
      Object.assign(formData, {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
    }
  },
  { immediate: true }
)

// 监听内部状态变化
watch(
  () => showModal.value,
  (newVal) => {
    emit('update:show', newVal)
  }
)

// 取消
const handleCancel = () => {
  showModal.value = false
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    await authAPI.changePassword({
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword
    })
    
    message.success('密码修改成功')
    showModal.value = false
  } catch (error) {
    if (error.message) {
      message.error(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
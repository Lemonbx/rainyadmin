<template>
  <div class="login-container">
    <n-card class="login-card" title="管理员登录">
      <n-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
      >
        <n-form-item label="用户名" path="loginname">
          <n-input 
            v-model:value="loginForm.loginname" 
            placeholder="请输入用户名"
            @keydown.enter="handleLogin"
          />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="loginForm.password"
            type="password"
            show-password-on="mousedown"
            placeholder="请输入密码"
            @keydown.enter="handleLogin"
          />
        </n-form-item>
        <n-form-item>
          <n-button
            type="primary"
            block
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMessage } from 'naive-ui'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  loginname: '',
  password: ''
})

const loginRules = {
  loginname: [
    {
      required: true,
      message: '请输入用户名',
      trigger: ['input', 'blur']
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: ['input', 'blur']
    }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()
    loading.value = true
    
    await userStore.login(loginForm)
    await userStore.getUserInfo()
    
    message.success('登录成功')
    router.push('/dashboard')
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
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #ffffff;
}

.login-card {
  width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
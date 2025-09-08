import axios from 'axios'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['auth'] = userStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果code为1，表示成功
    if (res.code === 1) {
      return res
    } else if (res.code === 401) {
      // 未授权，跳转到登录页
      const userStore = useUserStore()
      userStore.logout()
      const router = useRouter()
      router.push('/login')
      return Promise.reject(new Error(res.msg || '未授权'))
    } else {
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      const router = useRouter()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default request
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/admin',
  timeout: 30000,
})

// Request interceptor - add token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - unified error handling
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      if (status === 401) {
        ElMessage.error('未登录或登录已过期')
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error(data?.message || '无操作权限')
      } else if (status === 404) {
        ElMessage.error('资源不存在')
      } else if (status === 405) {
        ElMessage.error('请求方法不被允许')
      } else {
        ElMessage.error(data?.message || `请求错误 (${status})`)
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request

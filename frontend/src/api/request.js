import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

let refreshing = null

function rawPost(url, data) {
  return axios.post('/api/v1' + url, data, { timeout: 10000 }).then((r) => r.data)
}

async function tryRefresh() {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) return false
  if (!refreshing) {
    refreshing = rawPost('/auth/refresh', { refreshToken })
      .then(async (body) => {
        if (body.code === 0 && body.data) {
          const { useAuthStore } = await import('../stores/auth')
          useAuthStore().setLogin(body.data)
          return true
        }
        return false
      })
      .catch(() => false)
      .finally(() => {
        setTimeout(() => (refreshing = null), 100)
      })
  }
  return refreshing
}

async function forceLogout() {
  const { useAuthStore } = await import('../stores/auth')
  useAuthStore().clear()
  if (router.currentRoute.value.path !== '/checkin' && router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  async (resp) => {
    const body = resp.data
    if (body && body.code === 401) {
      if (resp.config._retried) {
        await forceLogout()
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(new Error('unauthorized'))
      }
      const ok = await tryRefresh()
      if (ok) {
        resp.config._retried = true
        return request(resp.config)
      }
      await forceLogout()
      ElMessage.error(body.msg || '登录已过期，请重新登录')
      return Promise.reject(new Error(body.msg || 'unauthorized'))
    }
    if (body && body.code !== 0) {
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg))
    }
    return body.data
  },
  (err) => {
    ElMessage.error(err.message || '网络异常')
    return Promise.reject(err)
  },
)

export default request

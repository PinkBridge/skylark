import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getAccessToken, clearTokens } from '@/utils/auth'
import i18n from '@/i18n/index'
import { getTenant } from '@/utils/tenant'

// In vue-i18n@9, use i18n.global.t instead of $t
const t = (key) => i18n.global.t(key)

const BASE_URL = process.env.VUE_APP_API_BASE_URL || '/api'

const service = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (!token && !config._retried) {
      config._retried = true
      return new Promise((resolve) => {
        setTimeout(() => {
          config.headers.Authorization = `Bearer ${getAccessToken() || ''}`
          resolve(config)
        }, 500)
      })
    }
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      config.headers['X-Tenant-Id'] = getTenant()?.id
    }
    // Add language header
    const locale = i18n.global.locale.value
    config.headers['X-Language'] = locale
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    if (response.data.code === 200) {
      return response.data.data
    } else if (response.data.code === 401) {
      ElMessage.error(t('UnauthorizedNotice'))
      clearTokens()
      window.location.href = '/welcome'
    } else if (response.data.code === 403) {
      ElMessage.error(t('AccessDeniedNotice'))
      return Promise.reject(response.data.message)
    } else {
      ElMessage.error(response.data.message)
      return Promise.reject(response.data.message)
    } 
  },
  (error) => {
    const { response } = error
    let message = t('RequestFailedNotice')

    if (response) {
      const { status, data } = response
      message = data?.message || data?.msg || message
      if (status === 401) {
        message = t('UnauthorizedNotice')
        clearTokens()
        window.location.href = '/welcome'
      }
    } else if (error.message?.includes('timeout')) {
      message = t('RequestTimeoutNotice')
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

const http = {
  get(url, params = {}, config = {}) {
    return service.get(url, { params, ...config })
  },
  post(url, data = {}, config = {}) {
    return service.post(url, data, config)
  },
  put(url, data = {}, config = {}) {
    return service.put(url, data, config)
  },
  patch(url, data = {}, config = {}) {
    return service.patch(url, data, config)
  },
  delete(url, params = {}, config = {}) {
    return service.delete(url, { params, ...config })
  }
}

export default http
export { service as axiosInstance }

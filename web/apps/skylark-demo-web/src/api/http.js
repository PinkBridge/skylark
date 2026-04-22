import axios from 'axios'
import { ElMessage } from 'element-plus'
import i18n from '@/i18n'
import { getAccessToken, clearTokens } from '@/utils/auth'
import { getTenant } from '@skylark/tenant-client'

const t = (key) => i18n.global.t(key)

const BASE_URL = process.env.VUE_APP_API_BASE_URL || '/api'

const service = axios.create({
  baseURL: BASE_URL,
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      config.headers['X-Tenant-Id'] = getTenant()?.id
    }
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
    const data = response.data
    if (data && data.code === 200) {
      return data.data
    }
    if (data && data.code === 401) {
      ElMessage.error(t('UnauthorizedNotice'))
      clearTokens()
      window.location.href = '/welcome'
      return Promise.reject(data.message)
    }
    if (data && data.code === 403) {
      ElMessage.error(t('AccessDeniedNotice'))
      return Promise.reject(data.message)
    }
    if (data && data.message) {
      ElMessage.error(data.message)
    }
    return Promise.reject(data?.message || 'error')
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
  delete(url, params = {}, config = {}) {
    return service.delete(url, { params, ...config })
  }
}

export default http
export { service as axiosInstance }

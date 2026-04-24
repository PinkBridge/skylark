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

function inOAuthCallbackWindow() {
  try {
    if (typeof window === 'undefined' || !window.location) return false
    return String(window.location.search || '').includes('code=')
  } catch (e) {
    return false
  }
}

function shouldLogoutOn401(data) {
  const msg = (data && (data.message || data.msg)) ? String(data.message || data.msg) : ''
  // Only treat certain 401s as "session expired/invalid".
  // These are produced by permission service AuthRetWriter / entry point.
  const fatalKeys = new Set([
    'expired.jwt.unauthorized',
    'check.jwt.exception',
    'UNAUTHORIZED',
    'user.not.login',
    'user.info.not.available'
  ])
  if (fatalKeys.has(msg)) return true
  // Some deployments translate messages; fall back to keyword match.
  const lower = msg.toLowerCase()
  if (lower.includes('expired') && lower.includes('jwt')) return true
  if (lower.includes('jwt') && (lower.includes('invalid') || lower.includes('malformed'))) return true
  if (lower.includes('token') && (lower.includes('expired') || lower.includes('invalid'))) return true
  return false
}

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
      // Strategy B: only logout when token is clearly invalid/expired.
      if (getAccessToken() && !inOAuthCallbackWindow() && shouldLogoutOn401(data)) {
        clearTokens()
        window.location.href = '/welcome'
      }
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
        if (getAccessToken() && !inOAuthCallbackWindow() && shouldLogoutOn401(data)) {
          clearTokens()
          window.location.href = '/welcome'
        }
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

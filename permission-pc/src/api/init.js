import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const INIT_PREFIX = '/' + APP_NAME + '/platform-init'

export function getPlatformInitState() {
  return http.get(`${INIT_PREFIX}/state`)
}

export function initializePlatform(data) {
  return http.post(`${INIT_PREFIX}/initialize`, data)
}


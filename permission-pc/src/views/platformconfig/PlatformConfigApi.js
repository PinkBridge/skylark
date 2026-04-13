import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const PREFIX = '/' + APP_NAME + '/platform-configs'

export function listPlatformConfigs() {
  return http.get(PREFIX)
}

export function updatePlatformConfig(configKey, configValue) {
  const key = encodeURIComponent(configKey)
  return http.put(`${PREFIX}/${key}`, { configValue })
}

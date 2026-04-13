import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const APP_PREFIX = "/" + APP_NAME + "/apps"

/**
 * Get app page
 */
export function getAppPage(currentPage, pageSize, params) {
  return http.get(`${APP_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get app by clientId
 */
export function getAppByClientId(clientId) {
  return http.get(`${APP_PREFIX}/${clientId}`)
}

/**
 * Delete app by clientId
 */
export function deleteAppByClientId(clientId) {
  return http.delete(`${APP_PREFIX}/${clientId}`)
}

/**
 * Update app by clientId
 */
export function updateAppByClientId(clientId, app) {
  return http.put(`${APP_PREFIX}/${clientId}`, app)
}

/**
 * Create app
 */
export function createApp(app) {
  return http.post(`${APP_PREFIX}`, app)
}


import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const API_PREFIX = "/" + APP_NAME + "/apis"

/**
 * Get API page
 */
export function getApiPage(currentPage, pageSize, params) {
  return http.get(`${API_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Tenant-side grantable api list (ceiling-limited)
 */
export function getGrantableApis() {
  return http.get(`${API_PREFIX}/grantable`, {})
}

/**
 * Tenant-side grantable api list (ceiling-limited), optional app filter
 */
export function getGrantableApisByApp(app) {
  return http.get(`${API_PREFIX}/grantable`, app ? { app } : {})
}

/**
 * Get API by id
 */
export function getApiById(id) {
  return http.get(`${API_PREFIX}/${id}`)
}

/**
 * Delete API by id
 */
export function deleteApiById(id) {
  return http.delete(`${API_PREFIX}/${id}`)
}

/**
 * Update API by id
 */
export function updateApiById(id, api) {
  return http.put(`${API_PREFIX}/${id}`, api)
}

/**
 * Create API
 */
export function createApi(api) {
  return http.post(`${API_PREFIX}`, api)
}


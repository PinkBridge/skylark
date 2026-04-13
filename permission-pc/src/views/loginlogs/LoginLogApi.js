import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const LOGIN_LOG_PREFIX = '/' + APP_NAME + '/login-logs'

/**
 * Get login log page
 */
export function getLoginLogPage(currentPage, pageSize, params) {
  return http.get(`${LOGIN_LOG_PREFIX}/page?page=${currentPage}&size=${pageSize}`, params)
}

/**
 * Get login log by id
 */
export function getLoginLogById(id) {
  return http.get(`${LOGIN_LOG_PREFIX}/${id}`)
}

/**
 * Delete login log by id
 */
export function deleteLoginLogById(id) {
  return http.delete(`${LOGIN_LOG_PREFIX}/${id}`)
}

/**
 * Update login log by id
 */
export function updateLoginLogById(id, loginLog) {
  return http.put(`${LOGIN_LOG_PREFIX}/${id}`, loginLog)
}

/**
 * Create login log
 */
export function createLoginLog(loginLog) {
  return http.post(`${LOGIN_LOG_PREFIX}`, loginLog)
}


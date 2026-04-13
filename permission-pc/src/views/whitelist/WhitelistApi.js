import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const WHITELIST_PREFIX = "/" + APP_NAME + "/whitelist"

/**
 * Get whitelist page
 */
export function getWhitelistPage(currentPage, pageSize, params) {
  return http.get(`${WHITELIST_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get whitelist by id
 */
export function getWhitelistById(id) {
  return http.get(`${WHITELIST_PREFIX}/${id}`)
}


/**
 * Delete whitelist by id
 */
export function deleteWhitelistById(id) {
  return http.delete(`${WHITELIST_PREFIX}/${id}`)
}

/**
 * Update whitelist by id
 */
export function updateWhitelistById(id, whitelist) {
  return http.put(`${WHITELIST_PREFIX}/${id}`, whitelist)
}

/**
 * Create whitelist
 */
export function createWhitelist(whitelist) {
  return http.post(`${WHITELIST_PREFIX}`, whitelist)
}


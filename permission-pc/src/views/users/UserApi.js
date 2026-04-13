import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const USER_PREFIX = "/" + APP_NAME + "/users"

/**
 * Get user information
 */
export function getUserPage(currentPage, pageSize, params) {
  return http.get(`${USER_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get user by id
 */
export function getUserById(id) {
  return http.get(`${USER_PREFIX}/${id}`)
}

/**
 * Delete user by id
 */
export function deleteUserById(id) {
  return http.delete(`${USER_PREFIX}/${id}`)
}

/**
 * Update user by id
 */
export function updateUserById(id, user) {
  return http.put(`${USER_PREFIX}/${id}`, user)
}

/**
 * Create user
 */
export function createUser(user) {
  return http.post(`${USER_PREFIX}`, user)
}
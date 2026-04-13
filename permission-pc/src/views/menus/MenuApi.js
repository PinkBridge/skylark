import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const MENU_PREFIX = "/" + APP_NAME + "/menus"

/**
 * Get menu list (tree structure)
 */
export function getMenuList(params) {
  return http.get(`${MENU_PREFIX}/tree`, { ...params })
}

/**
 * Tenant-side grantable menu tree (ceiling-limited)
 */
export function getGrantableMenuTree() {
  return http.get(`${MENU_PREFIX}/grantable/tree`)
}

/**
 * Get menu by id
 */
export function getMenuById(id) {
  return http.get(`${MENU_PREFIX}/${id}`)
}

/**
 * Delete menu by id
 */
export function deleteMenuById(id) {
  return http.delete(`${MENU_PREFIX}/${id}`)
}

/**
 * Update menu by id
 */
export function updateMenuById(id, menu) {
  return http.put(`${MENU_PREFIX}/${id}`, menu)
}

/**
 * Create menu
 */
export function createMenu(menu) {
  return http.post(`${MENU_PREFIX}`, menu)
}


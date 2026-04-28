import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const MENU_PREFIX = "/" + APP_NAME + "/menus"

/** 侧栏 / 可授权菜单树：与 sys_menu.app_code 一致（默认 permission-web） */
export const MENU_APP_CODE = process.env.VUE_APP_MENU_APP || 'permission-web'

/**
 * Get menu list (tree structure). 管理列表请传入 params.app（OAuth client_id）
 */
export function getMenuList(params = {}) {
  return http.get(`${MENU_PREFIX}/tree`, { ...params })
}

/**
 * Tenant-side grantable menu tree (ceiling-limited)。可传 app（OAuth client_id）与其它 query，未传 app 时回退 MENU_APP_CODE。
 */
export function getGrantableMenuTree(params = {}) {
  const query = { ...params }
  if (!query.app && MENU_APP_CODE) {
    query.app = MENU_APP_CODE
  }
  return http.get(`${MENU_PREFIX}/grantable/tree`, query)
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

/**
 * Import menus by JSON file (multipart)
 */
export function importMenus(appCode, file, dryRun = false) {
  const form = new FormData()
  form.append('appCode', appCode)
  form.append('file', file)
  // Do not set Content-Type manually for FormData (axios will add boundary)
  return http.post(`${MENU_PREFIX}/import?dryRun=${dryRun ? 'true' : 'false'}`, form)
}


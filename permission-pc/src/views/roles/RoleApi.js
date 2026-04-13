import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const ROLE_PREFIX = "/" + APP_NAME + "/roles"

/**
 * Get role page
 */
export function getRolePage(currentPage, pageSize, params) {
  return http.get(`${ROLE_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get role list (all roles)
 */
export function getRoleList(params) {
  return http.get(`${ROLE_PREFIX}`, { ...params })
}

/**
 * Get role by id
 */
export function getRoleById(id) {
  return http.get(`${ROLE_PREFIX}/${id}`)
}

/**
 * Delete role by id
 */
export function deleteRoleById(id) {
  return http.delete(`${ROLE_PREFIX}/${id}`)
}

/**
 * Update role by id
 */
export function updateRoleById(id, role) {
  return http.put(`${ROLE_PREFIX}/${id}`, role)
}

/**
 * Create role
 */
export function createRole(role) {
  return http.post(`${ROLE_PREFIX}`, role)
}

/**
 * POST /api/permission/roles/{roleId}/apis/{apiId}:toggle
 */
export function toggleRoleApi(roleId, apiId) {
  return http.post(`${ROLE_PREFIX}/${roleId}/apis/${apiId}:toggle`)
}

/**
 * 全量覆盖角色与 API 的绑定（先清空再写入传入的 id 列表）
 * POST /api/permission/roles/{roleId}/apis:bind  body: number[]
 */
export function bindRoleApis(roleId, apiIds) {
  return http.post(`${ROLE_PREFIX}/${roleId}/apis:bind`, apiIds == null ? [] : apiIds)
}

/**
 * 全量覆盖角色与数据域的绑定
 * POST /api/permission/roles/{roleId}/data-domains:bind  body: number[]
 */
export function bindRoleDataDomains(roleId, dataDomainIds) {
  return http.post(`${ROLE_PREFIX}/${roleId}/data-domains:bind`, dataDomainIds == null ? [] : dataDomainIds)
}

/**
 * POST /api/permission/roles/{roleId}/menus/{menuId}:toggle
 */
export function toggleRoleMenu(roleId, menuIds) {
  return http.post(`${ROLE_PREFIX}/${roleId}/menus:toggle`, { menuIds: menuIds })
}

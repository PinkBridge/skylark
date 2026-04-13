import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const DATA_DOMAIN_PREFIX = '/' + APP_NAME + '/data-domains'

/**
 * Get data domain page
 */
export function getDataDomainPage(currentPage, pageSize, params) {
  return http.get(
    `${DATA_DOMAIN_PREFIX}/page?page=${currentPage}&size=${pageSize}`,
    { ...params }
  )
}

/**
 * Get data domain by id
 */
export function getDataDomainById(id) {
  return http.get(`${DATA_DOMAIN_PREFIX}/${id}`)
}

/**
 * Create data domain
 */
export function createDataDomain(dataDomain) {
  return http.post(`${DATA_DOMAIN_PREFIX}`, dataDomain)
}

/**
 * Update data domain by id
 */
export function updateDataDomainById(id, dataDomain) {
  return http.put(`${DATA_DOMAIN_PREFIX}/${id}`, dataDomain)
}

/**
 * Delete data domain by id
 */
export function deleteDataDomainById(id) {
  return http.delete(`${DATA_DOMAIN_PREFIX}/${id}`)
}

/**
 * 租户侧可授权数据域；平台侧为全量。
 * forRoleId 为当前正在绑定的角色 id：若为租户管理员角色则返回本租户全部数据域，否则返回天花板内集合。
 * GET /api/permission/data-domains/grantable?forRoleId=
 */
export function getGrantableDataDomains(forRoleId) {
  const params = forRoleId != null && forRoleId !== '' ? { forRoleId } : {}
  return http.get(`${DATA_DOMAIN_PREFIX}/grantable`, params)
}


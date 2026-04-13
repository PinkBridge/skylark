import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const ORG_PREFIX = '/' + APP_NAME + '/orgs'

/**
 * Get org page
 */
export function getOrgPage(currentPage, pageSize, params) {
  return http.get(`${ORG_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get org list (tree structure)
 */
export function getOrgList(params) {
  return http.get(`${ORG_PREFIX}/tree`, { ...params })
}

/**
 * Get org by id
 */
export function getOrgById(id) {
  return http.get(`${ORG_PREFIX}/${id}`)
}

/**
 * Delete org by id
 */
export function deleteOrgById(id) {
  return http.delete(`${ORG_PREFIX}/${id}`)
}

/**
 * Update org by id
 */
export function updateOrgById(id, org) {
  return http.put(`${ORG_PREFIX}/${id}`, org)
}

/**
 * Create org
 */
export function createOrg(org) {
  return http.post(`${ORG_PREFIX}`, org)
}



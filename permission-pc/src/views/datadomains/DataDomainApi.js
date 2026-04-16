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
 * Role binding list should match the Data Domains page.
 * GET /api/permission/data-domains/grantable
 */
export function getGrantableDataDomains() {
  return http.get(`${DATA_DOMAIN_PREFIX}/grantable`)
}


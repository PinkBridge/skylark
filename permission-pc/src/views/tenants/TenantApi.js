import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const TENANT_PREFIX = '/' + APP_NAME + '/tenants'

/**
 * Get tenant page
 */
export function getTenantPage(currentPage, pageSize, params) {
  return http.get(`${TENANT_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get tenant by id
 */
export function getTenantById(id) {
  return http.get(`${TENANT_PREFIX}/${id}`)
}

/**
 * Get tenant initialization info (default org + default user)
 */
export function getTenantInitInfo(tenantId) {
  return http.get(`${TENANT_PREFIX}/${tenantId}/init-info`)
}

/**
 * Get current user's tenant
 */
export function getMyTenant() {
  return http.get(`${TENANT_PREFIX}/me`)
}

/**
 * Delete tenant by id
 */
export function deleteTenantById(id) {
  return http.delete(`${TENANT_PREFIX}/${id}`)
}

/**
 * Update tenant by id
 */
export function updateTenantById(id, tenant) {
  return http.put(`${TENANT_PREFIX}/${id}`, tenant)
}

/**
 * Update current user's tenant
 */
export function updateMyTenant(tenant) {
  return http.put(`${TENANT_PREFIX}/me`, tenant)
}

/**
 * Create tenant
 */
export function createTenant(tenant) {
  return http.post(`${TENANT_PREFIX}`, tenant)
}

/**
 * Create tenant admin user
 */
export function createTenantAdmin(tenantId, payload) {
  return http.post(`${TENANT_PREFIX}/${tenantId}/admin`, payload)
}

/**
 * Initialize tenant (create default org + default user)
 */
export function initializeTenant(tenantId, payload) {
  return http.post(`${TENANT_PREFIX}/${tenantId}/initialize`, payload)
}

/**
 * Get roles by tenant id (for tenant admin creation)
 */
export function getTenantRoles(tenantId) {
  return http.get(`${TENANT_PREFIX}/${tenantId}/roles`)
}

/**
 * Upload logo image
 * @param {File} file - Image file to upload
 */
export function uploadLogo(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post(`${TENANT_PREFIX}/logo/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

import http from '@/api/http'

// In gateway, permission service is routed under /api/permission/**
// iot-app axios baseURL is /api, so request path should start with /permission/...
const APP_NAME = process.env.APP_NAME || 'permission'
const RESOURCE_PREFIX = '/' + APP_NAME + '/resources'

/**
 * Upload file resource
 * @param {File} file - File to upload
 */
export function uploadResource(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post(`${RESOURCE_PREFIX}/upload`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


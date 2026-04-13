import http from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const PREFIX = '/' + APP_NAME + '/operation-logs'

export function getOperationLogPage(currentPage, pageSize, params) {
  return http.get(`${PREFIX}/page?page=${currentPage}&size=${pageSize}`, params)
}

export function getOperationLogById(id) {
  return http.get(`${PREFIX}/${id}`)
}

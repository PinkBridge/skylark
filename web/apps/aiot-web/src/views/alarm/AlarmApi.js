import http from '@/api/http'

const PREFIX = '/aiot-service/alarm'

export function listAlarmRules(params) {
  return http.get(`${PREFIX}/rules`, params)
}

export function createAlarmRule(data) {
  return http.post(`${PREFIX}/rules`, data)
}

export function getAlarmRule(id) {
  return http.get(`${PREFIX}/rules/${id}`)
}

export function updateAlarmRule(id, data) {
  return http.put(`${PREFIX}/rules/${id}`, data)
}

export function deleteAlarmRule(id) {
  return http.delete(`${PREFIX}/rules/${id}`)
}

export function listAlarmRecords(params) {
  return http.get(`${PREFIX}/records`, params)
}

export function getAlarmRecord(id) {
  return http.get(`${PREFIX}/records/${id}`)
}


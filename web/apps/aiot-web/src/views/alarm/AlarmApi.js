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

export function recoverAlarmRecord(id) {
  return http.post(`${PREFIX}/records/${id}/recover`)
}

export function listAlarmNotifyConfigs(params) {
  return http.get(`${PREFIX}/notify-configs`, params)
}

export function getAlarmNotifyConfig(id) {
  return http.get(`${PREFIX}/notify-configs/${id}`)
}

export function createAlarmNotifyConfig(data) {
  return http.post(`${PREFIX}/notify-configs`, data)
}

export function updateAlarmNotifyConfig(id, data) {
  return http.put(`${PREFIX}/notify-configs/${id}`, data)
}

export function deleteAlarmNotifyConfig(id) {
  return http.delete(`${PREFIX}/notify-configs/${id}`)
}

export function testAlarmNotifyConfig(id) {
  return http.post(`${PREFIX}/notify-configs/${id}/test`)
}

export function listAlarmNotifyDeliveries(params) {
  return http.get(`${PREFIX}/notify-deliveries`, params)
}


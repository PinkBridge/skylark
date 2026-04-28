import http from '@/api/http'

const API_PREFIX = '/aiot-service/mgmt'

export function listDeviceGroups(params) {
  return http.get(`${API_PREFIX}/device-groups`, params)
}

export function createDeviceGroup(data) {
  return http.post(`${API_PREFIX}/device-groups`, data)
}

export function updateDeviceGroup(groupKey, data) {
  return http.put(`${API_PREFIX}/device-groups/${groupKey}`, data)
}

export function deleteDeviceGroup(groupKey) {
  return http.delete(`${API_PREFIX}/device-groups/${groupKey}`)
}

export function listGroupDevices(groupKey) {
  return http.get(`${API_PREFIX}/device-groups/${groupKey}/devices`)
}

export function addDevicesToGroup(groupKey, devices) {
  return http.post(`${API_PREFIX}/device-groups/${groupKey}/devices`, { devices })
}

export function removeDeviceFromGroup(groupKey, productKey, deviceKey) {
  return http.delete(`${API_PREFIX}/device-groups/${groupKey}/devices/${productKey}/${deviceKey}`)
}


import http from '@/api/http'

const API_PREFIX = '/aiot-service/mgmt'

export function getThingModel(productKey) {
  return http.get(`${API_PREFIX}/products/${productKey}/thing-model`, {}, { suppressErrorMessage: true })
}

export function saveThingModel(productKey, data) {
  return http.put(`${API_PREFIX}/products/${productKey}/thing-model`, data)
}


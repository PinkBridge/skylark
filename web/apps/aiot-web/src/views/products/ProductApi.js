import http from '@/api/http'

const API_PREFIX = '/aiot-service/mgmt'

export function getProductList(params) {
  return http.get(`${API_PREFIX}/products`, params)
}

export function getProduct(productKey) {
  return http.get(`${API_PREFIX}/products/${productKey}`)
}

export function createProduct(data) {
  return http.post(`${API_PREFIX}/products`, data)
}

export function updateProduct(productKey, data) {
  return http.put(`${API_PREFIX}/products/${productKey}`, data)
}

export function deleteProduct(productKey) {
  return http.delete(`${API_PREFIX}/products/${productKey}`)
}

export function enableProduct(productKey) {
  return http.patch(`${API_PREFIX}/products/${productKey}/enable`)
}

export function disableProduct(productKey) {
  return http.patch(`${API_PREFIX}/products/${productKey}/disable`)
}

export function copyProduct(productKey, data) {
  return http.post(`${API_PREFIX}/products/${productKey}/copy`, data)
}

export function getProductDataChannels(productKey) {
  return http.get(`${API_PREFIX}/products/${productKey}/data-channels`)
}

export function updateProductDataChannel(productKey, id, data) {
  return http.patch(`${API_PREFIX}/products/${productKey}/data-channels/${id}`, data)
}


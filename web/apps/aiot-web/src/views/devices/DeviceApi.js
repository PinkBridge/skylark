import http, { axiosInstance } from '@/api/http'

const API_PREFIX = '/aiot-service/mgmt'

export function getDeviceList(productKey) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices`)
}

export function getAllDevices() {
  return http.get(`${API_PREFIX}/devices`)
}

/**
 * Download Excel template for bulk device import (authenticated).
 */
export async function downloadDeviceImportTemplate() {
  const res = await axiosInstance.get(`${API_PREFIX}/devices/import-template`, {
    responseType: 'blob'
  })
  const blob = res.data
  const headers = res.headers || {}
  const ct = headers['content-type'] || headers['Content-Type'] || ''
  if (ct.includes('application/json') || (blob.type && blob.type.includes('application/json'))) {
    const text = await blob.text()
    try {
      const j = JSON.parse(text)
      throw new Error(j.message || 'download failed')
    } catch (e) {
      if (e.message && e.message !== 'Unexpected token') {
        throw e
      }
      throw new Error(text || 'download failed')
    }
  }
  const cd = headers['content-disposition'] || headers['Content-Disposition'] || ''
  let filename = 'device-import-template.xlsx'
  const m = /filename\*=UTF-8''([^;\n]+)|filename="([^"]+)"|filename=([^;\n]+)/i.exec(cd)
  if (m) {
    filename = decodeURIComponent((m[1] || m[2] || m[3]).trim())
  }
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

/**
 * @returns {Promise<{ successCount: number, failCount: number, errors: Array<{rowNumber:number, message:string}> }>}
 */
export function importDevicesFromExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return axiosInstance.post(`${API_PREFIX}/devices/import`, formData)
}

export function listDevicesPage(params) {
  return http.get(`${API_PREFIX}/devices/page`, params)
}

export function getDevice(productKey, deviceKey) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}`)
}

export function createDevice(productKey, data) {
  return http.post(`${API_PREFIX}/products/${productKey}/devices`, data)
}

export function updateDevice(productKey, deviceKey, data) {
  return http.put(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}`, data)
}

export function deleteDevice(productKey, deviceKey) {
  return http.delete(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}`)
}

export function enableDevice(productKey, deviceKey) {
  return http.patch(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/enable`)
}

export function disableDevice(productKey, deviceKey) {
  return http.patch(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/disable`)
}

export function resetDeviceSecret(productKey, deviceKey) {
  return http.post(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/reset-secret`)
}

export function getDevicePropertyRecords(productKey, deviceKey, params) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/property-records`, params)
}

export function getDeviceCurrentProperties(productKey, deviceKey) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/current-properties`)
}

export function getLatestDevicePropertyValue(productKey, deviceKey, propertyIdentifier) {
  const encoded = encodeURIComponent(propertyIdentifier)
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/property-records/${encoded}/latest`)
}

export function getDeviceEventRecords(productKey, deviceKey, params) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/event-records`, params)
}

export function getDeviceServiceRecords(productKey, deviceKey, params) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/service-records`, params)
}

export function getDeviceConnectRecords(productKey, deviceKey, params) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/connect-records`, params)
}

export function getDeviceDataChannels(productKey, deviceKey) {
  return http.get(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/data-channels`)
}

export function updateDeviceDataChannel(productKey, deviceKey, id, data) {
  return http.patch(`${API_PREFIX}/products/${productKey}/devices/${deviceKey}/data-channels/${id}`, data)
}


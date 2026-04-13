import http, { axiosInstance } from '@/api/http'

const APP_NAME = process.env.APP_NAME || 'permission'
const RESOURCE_PREFIX = '/' + APP_NAME + '/resources'

/**
 * Get resource page
 */
export function getResourcePage(currentPage, pageSize, params) {
  return http.get(`${RESOURCE_PREFIX}/page?page=${currentPage}&size=${pageSize}`, { ...params })
}

/**
 * Get resource by id
 */
export function getResourceById(id) {
  return http.get(`${RESOURCE_PREFIX}/${id}`)
}

/**
 * Delete resource by id
 */
export function deleteResourceById(id) {
  return http.delete(`${RESOURCE_PREFIX}/${id}`)
}

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

function parseFilenameFromContentDisposition(cd, fallback) {
  if (!cd) {
    return fallback || 'download'
  }
  const utf8 = /filename\*=UTF-8''([^;\s]+)/i.exec(cd)
  if (utf8) {
    try {
      return decodeURIComponent(utf8[1])
    } catch (e) {
      return utf8[1]
    }
  }
  const ascii = /filename="([^"]+)"/i.exec(cd)
  if (ascii) {
    return ascii[1]
  }
  return fallback || 'download'
}

/**
 * 带 Token 下载文件（避免直接打开链接未带鉴权）
 */
export async function downloadResource(id, fallbackName) {
  const res = await axiosInstance.get(`${RESOURCE_PREFIX}/download/${id}`, { responseType: 'blob' })
  const blob = res.data
  const ct = (res.headers['content-type'] || '').toLowerCase()
  if (ct.includes('application/json')) {
    const text = await blob.text()
    let msg = 'Download failed'
    try {
      const j = JSON.parse(text)
      msg = j.message || msg
    } catch (e) {
      /* ignore */
    }
    throw new Error(msg)
  }
  const filename = parseFilenameFromContentDisposition(res.headers['content-disposition'], fallbackName)
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/**
 * 拉取预览用 Blob（图片等 inline展示）
 */
export async function fetchResourcePreviewBlob(id) {
  const res = await axiosInstance.get(`${RESOURCE_PREFIX}/preview/${id}`, { responseType: 'blob' })
  const blob = res.data
  const ct = (res.headers['content-type'] || '').toLowerCase()
  if (ct.includes('application/json')) {
    const text = await blob.text()
    let msg = 'Preview failed'
    try {
      const j = JSON.parse(text)
      msg = j.message || msg
    } catch (e) {
      /* ignore */
    }
    throw new Error(msg)
  }
  return blob
}


import http from '@/api/http'

const BASE = '/business-service/blog/posts'

export function getBlogPostPage(page, size, params = {}) {
  return http.get(`${BASE}/page`, { page, size, ...params })
}

export function getBlogPostById(id) {
  return http.get(`${BASE}/${id}`)
}

export function createBlogPost(data) {
  return http.post(BASE, data)
}

export function updateBlogPostById(id, data) {
  return http.put(`${BASE}/${id}`, data)
}

export function deleteBlogPostById(id) {
  return http.delete(`${BASE}/${id}`)
}


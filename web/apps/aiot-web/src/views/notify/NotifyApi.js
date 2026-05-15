import http from '@/api/http'

const PREFIX = '/aiot-service/notify'

export function listNotifyChannels(params) {
  return http.get(`${PREFIX}/channels`, params)
}

export function listNotifyChannelOptions(channelKind) {
  return http.get(`${PREFIX}/channels/options`, { channelKind })
}

export function createNotifyChannel(data) {
  return http.post(`${PREFIX}/channels`, data)
}

export function getNotifyChannel(id) {
  return http.get(`${PREFIX}/channels/${id}`)
}

export function updateNotifyChannel(id, data) {
  return http.put(`${PREFIX}/channels/${id}`, data)
}

export function deleteNotifyChannel(id) {
  return http.delete(`${PREFIX}/channels/${id}`)
}

export function testNotifyChannel(id, data) {
  return http.post(`${PREFIX}/channels/${id}/test`, data)
}

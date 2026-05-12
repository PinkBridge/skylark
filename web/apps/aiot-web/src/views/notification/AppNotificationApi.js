import http from '@/api/http'

const PREFIX = '/aiot-service/notification'

export function listEventTypes() {
  return http.get(`${PREFIX}/event-types`)
}

export function listChannels(params) {
  return http.get(`${PREFIX}/channels`, params)
}

export function getChannel(id) {
  return http.get(`${PREFIX}/channels/${id}`)
}

export function createChannel(data) {
  return http.post(`${PREFIX}/channels`, data)
}

export function updateChannel(id, data) {
  return http.put(`${PREFIX}/channels/${id}`, data)
}

export function deleteChannel(id) {
  return http.delete(`${PREFIX}/channels/${id}`)
}

export function testChannel(id) {
  return http.post(`${PREFIX}/channels/${id}/test`, {})
}

export function listSubscriptions(params) {
  return http.get(`${PREFIX}/subscriptions`, params)
}

export function getSubscription(id) {
  return http.get(`${PREFIX}/subscriptions/${id}`)
}

export function createSubscription(data) {
  return http.post(`${PREFIX}/subscriptions`, data)
}

export function updateSubscription(id, data) {
  return http.put(`${PREFIX}/subscriptions/${id}`, data)
}

export function deleteSubscription(id) {
  return http.delete(`${PREFIX}/subscriptions/${id}`)
}

export function listDeliveries(params) {
  return http.get(`${PREFIX}/deliveries`, params)
}

export function retryDelivery(id) {
  return http.post(`${PREFIX}/deliveries/${id}/retry`, {})
}

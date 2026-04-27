import { createOauthClient } from '@skylark/oauth-client'
import { getTenant } from '@skylark/tenant-client'

const oauth = createOauthClient({
  oauthBase: process.env.VUE_APP_OAUTH_BASE || '/oauth',
  clientId: process.env.VUE_APP_CLIENT_ID || 'permission-web',
  clientSecret: process.env.VUE_APP_CLIENT_SECRET || '112233',
  getRedirectUri() {
    if (typeof window === 'undefined' || !window.location) return '/home'
    const { protocol, host } = window.location
    return `${protocol}//${host}/home`
  }
})

export function getAuthorizationUrl() {
  return oauth.getAuthorizationUrl()
}

export function exchangeCodeForToken(code) {
  const tid = getTenant()?.id
  const headers =
    tid !== undefined && tid !== null && tid !== '' ? { 'X-Tenant-Id': tid } : {}
  return oauth.exchangeCodeForToken(code, headers)
}

export function checkToken(accessToken) {
  return oauth.checkToken(accessToken)
}

export function refreshToken(token) {
  return oauth.refreshToken(token, { 'X-Tenant-Id': getTenant()?.id })
}

export function logout(accessToken) {
  return oauth.logout(accessToken, { 'X-Tenant-Id': getTenant()?.id })
}

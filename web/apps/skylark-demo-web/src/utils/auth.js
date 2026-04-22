import {
  isAuthenticated,
  getAccessToken,
  getRefreshToken,
  saveTokens,
  clearTokens as clearOauthTokenEntries
} from '@skylark/oauth-client'
import { clearTenant } from '@skylark/tenant-client'

function clearBrowserCookies() {
  if (typeof document === 'undefined' || typeof window === 'undefined') {
    return
  }

  // Best-effort: delete cookies that JS can access (non-HttpOnly).
  const hostname = window.location.hostname
  const cookieDomains = new Set()
  cookieDomains.add(undefined)
  cookieDomains.add(hostname)
  if (hostname && hostname !== 'localhost' && hostname !== '127.0.0.1') {
    cookieDomains.add(`.${hostname}`)
  }

  const paths = ['/', window.location.pathname || '/']

  const raw = document.cookie ? document.cookie.split(';') : []
  for (const part of raw) {
    const eq = part.indexOf('=')
    const name = (eq >= 0 ? part.slice(0, eq) : part).trim()
    if (!name) {
      continue
    }
    for (const domain of cookieDomains) {
      for (const path of paths) {
        const base = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=${path}`
        document.cookie = domain ? `${base};domain=${domain}` : base
        document.cookie = `${base};Secure`
        document.cookie = domain ? `${base};domain=${domain};Secure` : `${base};Secure`
      }
    }
  }
}

/**
 * Full client-side session cleanup (tokens, userInfo, tenant cache, storage, cookies).
 * Aligns with permission-app {@code clearTokens}.
 */
export function clearTokens() {
  clearOauthTokenEntries()
  clearTenant()

  try {
    localStorage.clear()
  } catch (e) {
    /* ignore */
  }
  try {
    sessionStorage.clear()
  } catch (e) {
    /* ignore */
  }

  clearBrowserCookies()
}

export {
  isAuthenticated,
  getAccessToken,
  getRefreshToken,
  saveTokens
}

export function saveUserInfo(userInfo, remember = false) {
  const storage = remember ? localStorage : sessionStorage
  storage.setItem('userInfo', JSON.stringify(userInfo))
}

export function getUserInfo() {
  const raw = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

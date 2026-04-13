/**
 * Authentication utility functions
 */

// Memory cache for tokens to avoid timing issues
let tokenCache = {
  accessToken: null,
  refreshToken: null
}

function clearBrowserCookies() {
  if (typeof document === 'undefined' || typeof window === 'undefined') {
    return
  }

  // Best-effort: delete cookies that JS can access (non-HttpOnly).
  // HttpOnly cookies (common for server sessions) cannot be cleared from JS.
  const hostname = window.location.hostname
  const cookieDomains = new Set()
  cookieDomains.add(undefined) // host-only cookies
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
 * Check if user is authenticated
 */
export function isAuthenticated() {
  return !!(tokenCache.accessToken || localStorage.getItem('access_token') || sessionStorage.getItem('access_token'))
}

/**
 * Get access token
 * Priority: memory cache > localStorage > sessionStorage
 */
export function getAccessToken() {
  if (tokenCache.accessToken) {
    return tokenCache.accessToken
  }
  const token = localStorage.getItem('access_token') || sessionStorage.getItem('access_token')
  if (token) {
    tokenCache.accessToken = token
  }
  return token
}

/**
 * Get refresh token
 * Priority: memory cache > localStorage > sessionStorage
 */
export function getRefreshToken() {
  if (tokenCache.refreshToken) {
    return tokenCache.refreshToken
  }
  const token = localStorage.getItem('refresh_token') || sessionStorage.getItem('refresh_token')
  if (token) {
    tokenCache.refreshToken = token
  }
  return token
}

/**
 * Save tokens
 * @param {string} accessToken - Access token
 * @param {string} refreshToken - Refresh token (optional)
 * @param {boolean} remember - Whether to remember (use localStorage)
 */
export function saveTokens(accessToken, refreshToken = null, remember = false) {
  // Update memory cache first for immediate availability
  tokenCache.accessToken = accessToken
  if (refreshToken) {
    tokenCache.refreshToken = refreshToken
  }
  
  // Then save to storage
  const storage = remember ? localStorage : sessionStorage
  storage.setItem('access_token', accessToken)
  if (refreshToken) {
    storage.setItem('refresh_token', refreshToken)
  }
}

/**
 * Clear tokens
 */
export function clearTokens() {
  tokenCache.accessToken = null
  tokenCache.refreshToken = null
  
  // Clear all browser storage for this origin (logout / forced re-login).
  // This avoids leaving stale tenant/permission caches behind.
  try {
    localStorage.clear()
  } catch (e) {
    // ignore
  }
  try {
    sessionStorage.clear()
  } catch (e) {
    // ignore
  }

  clearBrowserCookies()
}

/**
 * Save user information
 * @param {object} userInfo - User information
 * @param {boolean} remember - Whether to remember
 */
export function saveUserInfo(userInfo, remember = false) {
  const storage = remember ? localStorage : sessionStorage
  storage.setItem('userInfo', JSON.stringify(userInfo))
}

/**
 * Get user information
 */
export function getUserInfo() {
  const userInfoStr = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      return JSON.parse(userInfoStr)
    } catch (e) {
      return null
    }
  }
  return null
}

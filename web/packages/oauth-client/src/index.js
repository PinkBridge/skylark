import axios from 'axios'

const ACCESS_TOKEN_KEY = 'skylark.access_token'
const REFRESH_TOKEN_KEY = 'skylark.refresh_token'
const EXPIRES_AT_KEY = 'skylark.expires_at_ms'

export function saveTokens(tokenResponse) {
  if (!tokenResponse) return
  const access = tokenResponse.access_token || tokenResponse.accessToken
  const refresh = tokenResponse.refresh_token || tokenResponse.refreshToken
  const expiresIn = tokenResponse.expires_in || tokenResponse.expiresIn

  if (access) localStorage.setItem(ACCESS_TOKEN_KEY, access)
  if (refresh) localStorage.setItem(REFRESH_TOKEN_KEY, refresh)
  if (typeof expiresIn === 'number') {
    localStorage.setItem(EXPIRES_AT_KEY, String(Date.now() + expiresIn * 1000))
  }
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(EXPIRES_AT_KEY)
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function isAuthenticated() {
  const token = getAccessToken()
  if (!token) return false
  const expiresAt = Number(localStorage.getItem(EXPIRES_AT_KEY) || 0)
  return !expiresAt || Date.now() < expiresAt
}

function toFormUrlEncoded(body) {
  const params = new URLSearchParams()
  Object.keys(body).forEach((k) => {
    const v = body[k]
    if (v === undefined || v === null) return
    params.append(k, String(v))
  })
  return params
}

/**
 * OAuth2 confidential client authentication via HTTP Basic (RFC 7617).
 * Using this on /oauth/token and /oauth/check_token avoids 401 responses that
 * include WWW-Authenticate: Basic on some stacks — which can trigger the
 * browser's native credential dialog for navigational or mixed requests.
 */
function oauthClientBasicHeaders(clientId, clientSecret) {
  const secret =
    clientSecret === undefined || clientSecret === null ? '' : String(clientSecret)
  const raw = `${clientId}:${secret}`
  let b64
  if (typeof btoa !== 'undefined') {
    b64 = btoa(unescape(encodeURIComponent(raw)))
  } else if (typeof Buffer !== 'undefined') {
    b64 = Buffer.from(raw, 'utf8').toString('base64')
  } else {
    throw new Error('oauth-client: cannot encode Basic auth (no btoa/Buffer)')
  }
  return { Authorization: `Basic ${b64}` }
}

export function createOauthClient(options) {
  const {
    oauthBase = '/oauth',
    clientId,
    clientSecret,
    getRedirectUri
  } = options || {}

  if (!clientId) throw new Error('createOauthClient: clientId is required')

  const base = oauthBase.replace(/\/$/, '')

  function getAuthorizationUrl(params = {}) {
    const redirectUri = (getRedirectUri && getRedirectUri()) || params.redirectUri
    const qs = new URLSearchParams({
      response_type: 'code',
      client_id: clientId,
      redirect_uri: redirectUri || '',
      ...params
    })
    // Legacy Spring Security OAuth2 endpoint (oauthBase already contains `/oauth`)
    return `${base}/authorize?${qs.toString()}`
  }

  async function exchangeCodeForToken(code, headers = {}) {
    const redirectUri = getRedirectUri ? getRedirectUri() : ''
    const form = toFormUrlEncoded({
      grant_type: 'authorization_code',
      code,
      redirect_uri: redirectUri,
      client_id: clientId,
      client_secret: clientSecret
    })
    // Legacy Spring Security OAuth2 endpoint
    const { data } = await axios.post(`${base}/token`, form, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        ...oauthClientBasicHeaders(clientId, clientSecret),
        ...headers
      }
    })
    saveTokens(data)
    return data
  }

  async function refreshToken(refreshTokenValue, headers = {}) {
    const form = toFormUrlEncoded({
      grant_type: 'refresh_token',
      refresh_token: refreshTokenValue,
      client_id: clientId,
      client_secret: clientSecret
    })
    const { data } = await axios.post(`${base}/token`, form, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        ...oauthClientBasicHeaders(clientId, clientSecret),
        ...headers
      }
    })
    saveTokens(data)
    return data
  }

  async function checkToken(accessToken, headers = {}) {
    const { data } = await axios.post(
      `${base}/check_token`,
      toFormUrlEncoded({
        token: accessToken,
        client_id: clientId,
        client_secret: clientSecret
      }),
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          ...oauthClientBasicHeaders(clientId, clientSecret),
          ...headers
        }
      }
    )
    return data
  }

  async function logout(accessToken, headers = {}) {
    // If your permission service exposes a different logout endpoint, adjust here.
    // Keep as best-effort.
    try {
      await axios.post(`${base}/logout`, null, {
        headers: { Authorization: `Bearer ${accessToken}`, ...headers }
      })
    } finally {
      clearTokens()
    }
  }

  return {
    getAuthorizationUrl,
    exchangeCodeForToken,
    refreshToken,
    checkToken,
    logout
  }
}

export function createAxiosApi(options) {
  const {
    baseURL,
    getToken,
    getTenantId,
    getLocale,
    onUnauthorized,
    onForbidden,
    onErrorMessage
  } = options || {}

  const axiosInstance = axios.create({
    baseURL
  })

  axiosInstance.interceptors.request.use((config) => {
    const token = getToken && getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    const tenantId = getTenantId && getTenantId()
    if (tenantId) {
      config.headers = config.headers || {}
      config.headers['X-Tenant-Id'] = tenantId
    }
    const locale = getLocale && getLocale()
    if (locale) {
      config.headers = config.headers || {}
      config.headers['Accept-Language'] = locale
    }
    return config
  })

  axiosInstance.interceptors.response.use(
    (resp) => resp,
    (err) => {
      const status = err?.response?.status
      const msg = err?.response?.data?.message || err?.response?.data?.msg
      if (status === 401 && onUnauthorized) onUnauthorized(err)
      else if (status === 403 && onForbidden) onForbidden(err)
      else if (msg && onErrorMessage) onErrorMessage(msg, err)
      return Promise.reject(err)
    }
  )

  return {
    axiosInstance
  }
}


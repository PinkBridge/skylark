export function getTenant() {
  const tenantStr = localStorage.getItem('tenant')
  if (!tenantStr) {
    return null
  }
  try {
    return JSON.parse(tenantStr)
  } catch (error) {
    console.error('Error parsing tenant from localStorage:', error)
    return null
  }
}

export function setTenant(tenant) {
  localStorage.setItem('tenant', JSON.stringify(tenant))
}

export function removeTenant() {
  localStorage.removeItem('tenant')
}

/**
 * 按域名（或 host，含端口）拉取租户；路径段编码，避免 127.0.0.1:9528 中的冒号破坏路由。
 */
function fetchTenantByDomainKey(domainKey) {
  if (!domainKey) {
    return Promise.resolve(null)
  }
  const segment = encodeURIComponent(domainKey)
  return fetch(`${process.env.VUE_APP_API_BASE_URL}/permission/tenants/domain/${segment}`)
    .then((response) => {
      if (response.ok) {
        return response.json()
      }
      return null
    })
    .then((data) => {
      if (data && data.data) {
        return data.data
      }
      return null
    })
    .catch((error) => {
      console.error('Error fetching tenant:', error)
      return null
    })
}

export function getTenantByDomain(domain) {
  return fetchTenantByDomainKey(domain).then((tenantData) => {
    if (tenantData) {
      setTenant(tenantData)
    }
    return tenantData
  })
}

/**
 * 按当前浏览器地址解析租户（与库中 sys_tenant.domain 一致）：
 * 1) location.host（含端口，如 127.0.0.1:9528）— 与「域名里写了端口」的配置一致；
 * 2) location.hostname（无端口，如 127.0.0.1）；
 * 3) 仅在本机回环 IP 仍失败时，再试 localhost（兼容种子默认租户）。
 */
export function getTenantForCurrentLocation() {
  const host = window.location.host
  const hostname = window.location.hostname

  return fetchTenantByDomainKey(host).then((data) => {
    if (data) {
      setTenant(data)
      return data
    }
    if (host !== hostname) {
      return fetchTenantByDomainKey(hostname).then((d2) => {
        if (d2) {
          setTenant(d2)
          return d2
        }
        return tryLocalhostFallback(hostname)
      })
    }
    return tryLocalhostFallback(hostname)
  })
}

function tryLocalhostFallback(hostname) {
  const hl = (hostname || '').toLowerCase()
  if (hl === '127.0.0.1' || hl === '::1' || hl === '[::1]') {
    return fetchTenantByDomainKey('localhost').then((d3) => {
      if (d3) {
        setTenant(d3)
      }
      return d3
    })
  }
  return Promise.resolve(null)
}

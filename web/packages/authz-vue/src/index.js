function flattenMenu(tree) {
  const out = []
  const stack = Array.isArray(tree) ? [...tree] : []
  while (stack.length) {
    const n = stack.shift()
    if (!n) continue
    out.push(n)
    const children = n.children || n.childrens || n.items
    if (Array.isArray(children)) stack.unshift(...children)
  }
  return out
}

export function createHasPermission(options) {
  const { fetchMenuTree, getAccessToken } = options || {}
  let loaded = false
  let loadingPromise = null
  let permSet = new Set()

  async function ensureLoaded() {
    if (loaded) return
    if (typeof getAccessToken === 'function' && !getAccessToken()) {
      return
    }
    if (loadingPromise) return loadingPromise
    loadingPromise = Promise.resolve()
      .then(() => (fetchMenuTree ? fetchMenuTree() : []))
      .then((tree) => {
        const nodes = flattenMenu(tree)
        const labels = nodes
          .map((x) => x.permlabel || x.permLabel || x.permission || x.code)
          .filter(Boolean)
        permSet = new Set(labels)
        loaded = true
      })
      .finally(() => {
        loadingPromise = null
      })
    return loadingPromise
  }

  return function hasPermission(permlabel) {
    if (!permlabel) return true
    if (typeof getAccessToken === 'function' && !getAccessToken()) {
      return false
    }
    if (!loaded) {
      // fire-and-forget load to avoid blocking rendering
      ensureLoaded()
      return false
    }
    if (Array.isArray(permlabel)) {
      return permlabel.every((p) => permSet.has(p))
    }
    return permSet.has(permlabel)
  }
}

export function createPermissionDirective(hasPermission) {
  function apply(el, binding) {
    const perm = binding.value
    const ok = hasPermission ? hasPermission(perm) : true
    el.style.display = ok ? '' : 'none'
  }

  return {
    mounted(el, binding) {
      apply(el, binding)
    },
    updated(el, binding) {
      apply(el, binding)
    }
  }
}


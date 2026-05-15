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
  const listeners = new Set()

  function notifyListeners() {
    for (const fn of [...listeners]) {
      try {
        fn()
      } catch {
        /* ignore subscriber errors */
      }
    }
  }

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
      .catch(() => {
        permSet = new Set()
        loaded = true
      })
      .finally(() => {
        loadingPromise = null
        notifyListeners()
      })
    return loadingPromise
  }

  function hasPermission(permlabel) {
    if (!permlabel) return true
    if (typeof getAccessToken === 'function' && !getAccessToken()) {
      return false
    }
    if (!loaded) {
      // fire-and-forget load; directive subscribers re-apply when load finishes
      ensureLoaded()
      return false
    }
    if (Array.isArray(permlabel)) {
      return permlabel.every((p) => permSet.has(p))
    }
    return permSet.has(permlabel)
  }

  /** Re-run permission UI (e.g. v-permission) after async menu tree load completes. */
  hasPermission.subscribe = (listener) => {
    if (typeof listener !== 'function') return () => {}
    listeners.add(listener)
    if (loaded) {
      try {
        listener()
      } catch {
        /* ignore */
      }
    }
    return () => listeners.delete(listener)
  }

  return hasPermission
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
      if (hasPermission && typeof hasPermission.subscribe === 'function') {
        el.__authzPermUnsub = hasPermission.subscribe(() => apply(el, binding))
      }
    },
    updated(el, binding) {
      apply(el, binding)
    },
    unmounted(el) {
      if (el.__authzPermUnsub) {
        el.__authzPermUnsub()
        delete el.__authzPermUnsub
      }
    }
  }
}

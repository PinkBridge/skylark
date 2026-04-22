export function resolvePostLoginRouteName(router, options = {}) {
  const preferred = Array.isArray(options.preferred) && options.preferred.length
    ? options.preferred
    : ['Apps']
  const fallback = options.fallback || 'Home'

  if (!router || typeof router.hasRoute !== 'function') {
    return fallback
  }

  for (const name of preferred) {
    if (name && router.hasRoute(name)) {
      return name
    }
  }

  return fallback
}


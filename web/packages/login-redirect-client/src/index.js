/**
 * Resolve the target route name after OAuth login.
 *
 * Default behavior:
 * - If router has a route named "Apps", redirect there (permission-app apps page).
 * - Otherwise fallback to the provided `fallback` (typically "Home").
 */
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


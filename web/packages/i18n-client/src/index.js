/**
 * Resolve default locale for Skylark frontends.
 *
 * Precedence:
 * 1) localStorage[storageKey] if supported & valid
 * 2) navigator.languages[0] / navigator.language
 * 3) fallback
 */

export function resolveDefaultLocale(options = {}) {
  const supported = Array.isArray(options.supported) && options.supported.length
    ? options.supported
    : ['zh', 'en']
  const storageKey = options.storageKey || 'locale'
  const fallback = options.fallback || 'zh'

  // 1) saved preference
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      const saved = localStorage.getItem(storageKey)
      if (saved && supported.includes(saved)) {
        return saved
      }
    }
  } catch (e) {
    /* ignore */
  }

  // 2) browser language
  try {
    const lang = (navigator.languages && navigator.languages[0]) || navigator.language || ''
    const v = String(lang).toLowerCase()
    if (supported.includes('zh') && v.startsWith('zh')) return 'zh'
    if (supported.includes('en')) return 'en'
  } catch (e) {
    /* ignore */
  }

  // 3) fallback
  return supported.includes(fallback) ? fallback : supported[0]
}


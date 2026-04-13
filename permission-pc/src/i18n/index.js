import { createI18n } from 'vue-i18n'
import enMessages from './en'
import zhMessages from './zh'

// Read saved locale from localStorage, use default if not found
const getDefaultLocale = () => {
  try {
    if (typeof window !== 'undefined' && window.localStorage) {
      const saved = localStorage.getItem('locale')
      if (saved && (saved === 'zh' || saved === 'en')) {
        return saved
      }
    }
  } catch (e) {
    console.warn('Unable to access localStorage:', e)
  }
  return 'zh'
}

const defaultLocale = getDefaultLocale()

const messages = {
  en: enMessages,
  zh: zhMessages
}

const i18n = createI18n({
  locale: defaultLocale, // Default locale
  fallbackLocale: 'en', // Fallback locale
  messages: messages, // Pass messages directly
  legacy: false,
  globalInjection: true, // Allow using $t in templates
  missingWarn: false, // Disable warnings
  fallbackWarn: false // Disable fallback warnings
})

export default i18n
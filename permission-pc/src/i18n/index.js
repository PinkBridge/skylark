import { createI18n } from 'vue-i18n'
import enMessages from './en'
import zhMessages from './zh'
import { resolveDefaultLocale } from '@skylark/i18n-client'

const defaultLocale = resolveDefaultLocale({ supported: ['zh', 'en'], storageKey: 'locale', fallback: 'zh' })

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
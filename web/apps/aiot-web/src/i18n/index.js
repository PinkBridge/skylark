import { createI18n } from 'vue-i18n'
import { shellMessages } from '@skylark/admin-shell'
import { resolveDefaultLocale } from '@skylark/i18n-client'

const i18n = createI18n({
  locale: resolveDefaultLocale({ supported: ['zh', 'en'], storageKey: 'locale', fallback: 'zh' }),
  fallbackLocale: 'en',
  messages: {
    en: { ...shellMessages.en },
    zh: { ...shellMessages.zh }
  },
  legacy: false,
  globalInjection: true,
  missingWarn: false,
  fallbackWarn: false
})

export default i18n

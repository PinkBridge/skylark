import { createI18n } from 'vue-i18n'
import { shellMessages } from '@skylark/admin-shell'
import { resolveDefaultLocale } from '@skylark/i18n-client'
import iotEn from '@/i18n/iot/en'
import iotZh from '@/i18n/iot/zh'

const i18n = createI18n({
  locale: resolveDefaultLocale({ supported: ['zh', 'en'], storageKey: 'locale', fallback: 'zh' }),
  fallbackLocale: 'en',
  messages: {
    en: { ...shellMessages.en, ...iotEn },
    zh: { ...shellMessages.zh, ...iotZh }
  },
  legacy: false,
  globalInjection: true,
  missingWarn: false,
  fallbackWarn: false
})

export default i18n

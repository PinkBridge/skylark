import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import en from 'element-plus/dist/locale/en.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from '@/App.vue'
import router from '@/router'
import i18n from '@/i18n'
import { ADMIN_SHELL_CONFIG } from '@skylark/admin-shell'
import { createAdminShellConfig } from '@/adminShellConfig'
import permission from '@/directives/permission'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  if (component) {
    app.component(key, component)
  }
}

app.provide(ADMIN_SHELL_CONFIG, createAdminShellConfig())
app.directive('permission', permission)
app.use(i18n)
app.use(ElementPlus, { locale: en })
app.use(router)
app.mount('#app')

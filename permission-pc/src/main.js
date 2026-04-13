import { createApp } from 'vue'
import App from '@/App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import en from 'element-plus/dist/locale/en.mjs'
import router from '@/router'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import i18n from '@/i18n/index'
import permission from '@/directives/permission'

const app = createApp(App)
 
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  if (component) {
    app.component(key, component)
  }
}

// Register permission directive
app.directive('permission', permission)

app.use(i18n)
app.use(ElementPlus, {
  locale: en
})
app.use(router)
app.mount('#app')
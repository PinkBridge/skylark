<template>
  <div class="language-select">
    <el-dropdown @command="handleCommand">
      <span class="el-dropdown-link">
        <img class="language-icon" src="@/assets/language.png" alt="Language" />
        Language
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="zh-cn" :icon="User">中文</el-dropdown-item>
          <el-dropdown-item command="en-us" :icon="Lock">English</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script>
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

export default {
  name: 'LanguageSelectComponent',
  components: { },
  setup() {
    const { locale } = useI18n()
    const handleCommand = (lang) => {
      const localeMap = {
        'zh-cn': 'zh',
        'en-us': 'en'
      }
      const targetLocale = localeMap[lang] || lang
      if (targetLocale === 'zh' || targetLocale === 'en') {
        locale.value = targetLocale
        localStorage.setItem('locale', targetLocale)
        ElMessage.success(`Language changed to ${targetLocale}`)
      } else {
        ElMessage.warning(`Unsupported language: ${targetLocale}`)
      }
    }

    return {
      handleCommand,
      User,
      Lock
    }
  }
}
</script>

<style scoped>
.language-select {
  display: flex;
  align-items: center;
  justify-content: center;
}

.language-icon {
  width: 26px;
  height: 26px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  border: none;
}
</style>
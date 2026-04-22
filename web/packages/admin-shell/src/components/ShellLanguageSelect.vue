<template>
  <div class="language-select">
    <el-dropdown @command="handleCommand">
      <span class="el-dropdown-link">
        <el-icon class="lang-icon"><Promotion /></el-icon>
        {{ t('LanguageLabel') }}
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="zh">{{ t('LanguageZh') }}</el-dropdown-item>
          <el-dropdown-item command="en">{{ t('LanguageEn') }}</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'

const { locale, t } = useI18n()

const handleCommand = (lang) => {
  if (lang === 'zh' || lang === 'en') {
    locale.value = lang
    try {
      localStorage.setItem('locale', lang)
    } catch (e) {
      /* ignore */
    }
    ElMessage.success(t('LanguageChanged'))
  } else {
    ElMessage.warning(t('LanguageUnsupported'))
  }
}
</script>

<style scoped>
.language-select {
  display: flex;
  align-items: center;
  justify-content: center;
}

.lang-icon {
  margin-right: 6px;
  vertical-align: middle;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.85);
  border: none;
}
</style>

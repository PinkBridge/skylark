<template>
  <el-card shadow="always">
    <el-alert
      :title="t('ThingModelEntryMovedNotice')"
      type="info"
      :closable="false"
    />
  </el-card>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>


<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('DeviceGroupKeyLabel')">
      <el-input v-model="formInline.groupKey" :placeholder="t('DeviceGroupKeyLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('DeviceGroupNameLabel')">
      <el-input v-model="formInline.name" :placeholder="t('DeviceGroupNameLabel')" clearable />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :icon="Search" @click="onSubmit">{{ t('QueryLabel') }}</el-button>
      <el-button type="default" :icon="RefreshRight" @click="onReset">{{ t('ResetLabel') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
/* global defineProps */
import { reactive } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const props = defineProps(['search', 'reset'])
const { t } = useI18n()

const formInline = reactive({
  groupKey: '',
  name: ''
})

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.groupKey = ''
  formInline.name = ''
  props.reset({ ...formInline })
}
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}

.demo-form-inline .el-select {
  --el-select-width: 220px;
}
</style>


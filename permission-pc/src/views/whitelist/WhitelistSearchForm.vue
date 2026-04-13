<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('MethodLabel')">
      <el-select v-model="formInline.method" :placeholder="t('MethodLabel')" clearable>
        <el-option v-for="item in methodOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('PathLabel')">
      <el-input v-model="formInline.path" :placeholder="t('PathLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('EnableLabel')">
      <el-select v-model="formInline.enabled" :placeholder="t('EnableLabel')" clearable>
        <el-option :label="t('Enable')" value="true" />
        <el-option :label="t('Disable')" value="false" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :icon="Search">{{ t('QueryLabel') }}</el-button>
      <el-button type="default" @click="onReset" :icon="RefreshRight">{{ t('ResetLabel') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const props = defineProps(['search', 'reset'])

const { t } = useI18n()

const methodOptions = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' }
]

const formInline = reactive({
  method: '',
  path: '',
  enabled: ''
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.method = ''
  formInline.path = ''
  formInline.enabled = ''
  props.reset(formInline)
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


<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('ProductKeyLabel')">
      <el-input v-model="formInline.productKey" :placeholder="t('ProductKeyLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('NameLabel')">
      <el-input v-model="formInline.name" :placeholder="t('NameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('StatusLabel')">
      <el-select v-model="formInline.status" :placeholder="t('StatusLabel')" clearable>
        <el-option :label="t('Enabled')" value="enabled" />
        <el-option :label="t('Disabled')" value="disabled" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :icon="Search">{{ t('QueryLabel') }}</el-button>
      <el-button type="default" @click="onReset" :icon="RefreshRight">{{ t('ResetLabel') }}</el-button>
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
  productKey: '',
  name: '',
  status: ''
})

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.productKey = ''
  formInline.name = ''
  formInline.status = ''
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


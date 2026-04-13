<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('NameLabel')">
      <el-input v-model="formInline.name" :placeholder="t('NameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('CodeLabel')">
      <el-input v-model="formInline.code" :placeholder="t('CodeLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('TypeLabel')">
      <el-select v-model="formInline.type" :placeholder="t('TypeLabel')" clearable style="width: 220px;">
        <el-option label="ALL" value="ALL" />
        <el-option label="TENANT" value="TENANT" />
        <el-option label="ORG_ALL" value="ORG_ALL" />
        <el-option label="ORG_AND_CHILD" value="ORG_AND_CHILD" />
        <el-option label="ORG_ONLY" value="ORG_ONLY" />
        <el-option label="SELF" value="SELF" />
        <el-option label="CUSTOM" value="CUSTOM" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('EnabledLabel')">
      <el-select v-model="formInline.enabled" :placeholder="t('EnabledLabel')" clearable style="width: 220px;">
        <el-option :label="t('Yes')" :value="1" />
        <el-option :label="t('No')" :value="0" />
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

const formInline = reactive({
  name: '',
  code: '',
  type: '',
  enabled: ''
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.name = ''
  formInline.code = ''
  formInline.type = ''
  formInline.enabled = ''
  props.reset(formInline)
}
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}

.demo-form-inline .el-select {
  width: 220px;
}
</style>



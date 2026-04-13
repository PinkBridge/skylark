<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('NameLabel')">
      <el-input v-model="formInline.name" :placeholder="t('NameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('CodeLabel')">
      <el-input v-model="formInline.code" :placeholder="t('CodeLabel')" clearable />
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
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.name = ''
  formInline.code = ''
  props.reset(formInline)
}
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}
</style>



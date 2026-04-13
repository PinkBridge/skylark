<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('ClientIdLabel')">
      <el-input v-model="formInline.clientId" :placeholder="t('ClientIdLabel')" clearable />
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
  clientId: '',
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.clientId = ''
  props.reset(formInline)
}
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}
</style>


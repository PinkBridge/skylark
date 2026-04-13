<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('UsernameLabel')">
      <el-input v-model="formInline.username" :placeholder="t('UsernameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('HttpMethodLabel')">
      <el-input v-model="formInline.httpMethod" :placeholder="t('HttpMethodLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('HttpStatusLabel')">
      <el-input v-model="formInline.httpStatus" :placeholder="t('HttpStatusLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('RequestUriLabel')">
      <el-input v-model="formInline.requestUri" :placeholder="t('RequestUriLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('CreatedAtLabel')">
      <el-date-picker
        v-model="formInline.createdTime"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        clearable
      />
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
  username: '',
  httpMethod: '',
  httpStatus: '',
  requestUri: '',
  createdTime: ''
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.username = ''
  formInline.httpMethod = ''
  formInline.httpStatus = ''
  formInline.requestUri = ''
  formInline.createdTime = ''
  props.reset(formInline)
}
</script>

<style scoped>
.demo-form-inline .el-input {
  --el-input-width: 220px;
}
</style>

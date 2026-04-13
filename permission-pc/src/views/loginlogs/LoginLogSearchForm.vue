<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('UsernameLabel')">
      <el-input v-model="formInline.username" :placeholder="t('UsernameLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('LoginIpLabel')">
      <el-input v-model="formInline.loginIp" :placeholder="t('LoginIpLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('LoginStatusLabel')">
      <el-select v-model="formInline.loginStatus" :placeholder="t('LoginStatusLabel')" clearable style="width: 220px;">
        <el-option :label="t('Success')" value="SUCCESS" />
        <el-option :label="t('Failure')" value="FAILURE" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('ClientIdLabel')">
      <el-input v-model="formInline.clientId" :placeholder="t('ClientIdLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('LoginTimeLabel')">
      <el-date-picker
        v-model="formInline.loginTime"
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
  loginIp: '',
  loginStatus: '',
  clientId: '',
  loginTime: ''
})

const onSubmit = () => {
  props.search(formInline)
}

const onReset = () => {
  formInline.username = ''
  formInline.loginIp = ''
  formInline.loginStatus = ''
  formInline.clientId = ''
  formInline.loginTime = ''
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


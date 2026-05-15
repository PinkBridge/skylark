<template>
  <el-form :inline="true" :model="formInline" class="alarm-search-form">
    <el-form-item :label="t('AlarmNotifyFilterRule')">
      <el-select v-model="formInline.ruleId" filterable clearable :placeholder="t('AlarmRuleId')" style="width: 220px">
        <el-option :label="t('AllLabel')" :value="''" />
        <el-option v-for="r in ruleOptions" :key="r.id" :label="`${r.name} (#${r.id})`" :value="String(r.id)" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('AlarmNotifyFilterChannel')">
      <el-select v-model="formInline.channel" clearable :placeholder="t('AlarmNotifyFilterChannel')" style="width: 140px">
        <el-option :label="t('AllLabel')" value="" />
        <el-option :label="t('AlarmNotifyChannelEmail')" value="EMAIL" />
        <el-option :label="t('AlarmNotifyChannelSms')" value="SMS" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('AlarmNotifyFilterStatus')">
      <el-select v-model="formInline.status" clearable :placeholder="t('AlarmNotifyFilterStatus')" style="width: 140px">
        <el-option :label="t('AllLabel')" value="" />
        <el-option :label="t('AlarmNotifyStatusSuccess')" value="SUCCESS" />
        <el-option :label="t('AlarmNotifyStatusFailed')" value="FAILED" />
      </el-select>
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

const props = defineProps({
  search: { type: Function, required: true },
  reset: { type: Function, required: true },
  ruleOptions: { type: Array, default: () => [] }
})

const { t } = useI18n()
const formInline = reactive({ ruleId: '', channel: '', status: '' })

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.ruleId = ''
  formInline.channel = ''
  formInline.status = ''
  props.reset({ ...formInline })
}
</script>

<style scoped>
.alarm-search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>

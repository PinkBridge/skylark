<template>
  <el-form :inline="true" :model="formInline" class="alarm-search-form">
    <el-form-item :label="t('IntegrationDeviceGroupsLabel')">
      <el-select v-model="formInline.deviceGroupKey" filterable clearable :placeholder="t('IntegrationDeviceGroupsLabel')" style="width: 220px">
        <el-option
          v-for="g in deviceGroupOptions"
          :key="g.groupKey"
          :label="g.name ? `${g.name} (#${g.groupKey})` : `#${g.groupKey}`"
          :value="g.groupKey"
        />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('AlarmRuleId')">
      <el-input v-model="formInline.ruleId" clearable :placeholder="t('AlarmRuleId')" style="width: 220px" />
    </el-form-item>
    <el-form-item :label="t('AlarmSeverity')">
      <el-select v-model="formInline.severity" clearable :placeholder="t('AlarmSeverity')" style="width: 220px">
        <el-option :label="t('AllLabel')" value="" />
        <el-option label="HIGH" value="HIGH" />
        <el-option label="MEDIUM" value="MEDIUM" />
        <el-option label="LOW" value="LOW" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('StatusLabel')">
      <el-input v-model="formInline.status" clearable :placeholder="t('StatusLabel')" style="width: 220px" />
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
  deviceGroupOptions: { type: Array, default: () => [] }
})

const { t } = useI18n()

const formInline = reactive({
  deviceGroupKey: '',
  ruleId: '',
  severity: '',
  status: ''
})

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.deviceGroupKey = ''
  formInline.ruleId = ''
  formInline.severity = ''
  formInline.status = ''
  props.reset({ ...formInline })
}
</script>

<style scoped>
.alarm-search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>

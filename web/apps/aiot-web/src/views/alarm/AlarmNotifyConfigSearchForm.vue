<template>
  <el-form :inline="true" :model="formInline" class="alarm-search-form">
    <el-form-item :label="t('AlarmNotifyFilterRule')">
      <el-select v-model="formInline.ruleId" filterable clearable :placeholder="t('AlarmRuleId')" style="width: 240px">
        <el-option :label="t('AllLabel')" :value="''" />
        <el-option v-for="r in ruleOptions" :key="r.id" :label="`${r.name} (#${r.id})`" :value="String(r.id)" />
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
const formInline = reactive({ ruleId: '' })

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.ruleId = ''
  props.reset({ ...formInline })
}
</script>

<style scoped>
.alarm-search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>

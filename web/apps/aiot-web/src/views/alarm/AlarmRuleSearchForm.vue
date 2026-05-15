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
    <el-form-item :label="t('StatusLabel')">
      <el-select v-model="formInline.enabled" clearable :placeholder="t('StatusLabel')" style="width: 220px">
        <el-option :label="t('AllLabel')" value="" />
        <el-option :label="t('Enabled')" :value="true" />
        <el-option :label="t('Disabled')" :value="false" />
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
  deviceGroupOptions: { type: Array, default: () => [] }
})

const { t } = useI18n()

const formInline = reactive({
  deviceGroupKey: '',
  enabled: ''
})

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.deviceGroupKey = ''
  formInline.enabled = ''
  props.reset({ ...formInline })
}
</script>

<style scoped>
.alarm-search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>

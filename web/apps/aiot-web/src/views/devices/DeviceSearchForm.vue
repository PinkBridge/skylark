<template>
  <el-form :inline="true" :model="formInline" class="demo-form-inline">
    <el-form-item :label="t('ProductLabel')">
      <el-select
        v-model="formInline.productKey"
        :placeholder="t('SelectProductPlaceholder')"
        clearable
        @change="onProductChange"
      >
        <el-option v-for="item in products" :key="item.productKey" :label="item.name" :value="item.productKey" />
      </el-select>
    </el-form-item>
    <el-form-item :label="t('DeviceKeyLabel')">
      <el-input v-model="formInline.deviceKey" :placeholder="t('DeviceKeyLabel')" clearable />
    </el-form-item>
    <el-form-item :label="t('DeviceNameLabel')">
      <el-input v-model="formInline.deviceName" :placeholder="t('DeviceNameLabel')" clearable />
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
import { reactive, watch } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  products: {
    type: Array,
    default: () => []
  },
  productKey: {
    type: String,
    default: ''
  },
  search: Function,
  reset: Function,
  productChange: Function
})

const { t } = useI18n()

const formInline = reactive({
  productKey: props.productKey,
  deviceKey: '',
  deviceName: '',
  status: ''
})

watch(() => props.productKey, (value) => {
  formInline.productKey = value
})

const onSubmit = () => {
  props.search({ ...formInline })
}

const onReset = () => {
  formInline.productKey = ''
  formInline.deviceKey = ''
  formInline.deviceName = ''
  formInline.status = ''
  props.reset({ ...formInline })
}

const onProductChange = (value) => {
  props.productChange?.(value || '')
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


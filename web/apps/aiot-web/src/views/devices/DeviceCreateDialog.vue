<template>
  <el-dialog :model-value="visible" :title="$t('CreateTitle')" align-center destroy-on-close :show-close="false" style="max-width: 700px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="$t('ProductLabel')" prop="productKey">
        <el-select v-model="form.productKey" style="width: 100%" :placeholder="$t('SelectProductPlaceholder')" filterable>
          <el-option v-for="item in products" :key="item.productKey" :label="`${item.name} (${item.productKey})`" :value="item.productKey" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('DeviceNameLabel')" prop="deviceName">
        <el-input v-model="form.deviceName" :placeholder="$t('DeviceNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('DeviceAddressLabel')" prop="address">
        <el-input
          v-model="form.address"
          type="textarea"
          :rows="2"
          :placeholder="$t('DeviceAddressPlaceholder')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">{{ $t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { createDevice } from '@/views/devices/DeviceApi'

const props = defineProps({
  visible: Boolean,
  products: { type: Array, default: () => [] },
  productKey: { type: String, default: '' },
  onSubmit: Function,
  onCancel: Function
})
const { t } = useI18n()
const formRef = ref(null)
const form = ref({
  productKey: '',
  deviceName: '',
  address: ''
})

const rules = computed(() => ({
  productKey: [{ required: true, message: t('ProductRequiredForDevice'), trigger: 'change' }],
  deviceName: [{ required: true, message: t('DeviceNameRequired'), trigger: 'blur' }]
}))

watch(
  () => [props.visible, props.productKey],
  ([vis, pk]) => {
    if (vis) {
      form.value.productKey = pk || ''
    }
  }
)

const resetForm = () => {
  form.value = {
    productKey: props.productKey || '',
    deviceName: '',
    address: ''
  }
  formRef.value?.resetFields()
}

const handleCancel = () => {
  resetForm()
  props.onCancel()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const addr = form.value.address != null ? String(form.value.address).trim() : ''
    await createDevice(form.value.productKey, {
      deviceName: form.value.deviceName,
      ...(addr.length > 0 ? { address: addr } : {})
    })
    ElMessage.success(t('CreateSuccess'))
    resetForm()
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>


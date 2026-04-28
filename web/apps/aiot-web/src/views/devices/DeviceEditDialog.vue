<template>
  <el-dialog :model-value="visible" :title="$t('EditTitle')" align-center destroy-on-close :show-close="false" style="max-width: 700px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="$t('ProductKeyLabel')">
        <el-input v-model="form.productKey" disabled />
      </el-form-item>
      <el-form-item :label="$t('DeviceKeyLabel')">
        <el-input v-model="form.deviceKey" disabled />
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
        <el-button @click="props.onCancel()">{{ $t('CancelButtonText') }}</el-button>
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
import { getDevice, updateDevice } from '@/views/devices/DeviceApi'

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const formRef = ref(null)
const form = ref({})

const rules = computed(() => ({
  deviceName: [{ required: true, message: t('DeviceNameRequired'), trigger: 'blur' }]
}))

watch(
  () => [props.visible, props.row?.productKey, props.row?.deviceKey],
  async ([visible, productKey, deviceKey]) => {
    if (visible && productKey && deviceKey) {
      const data = await getDevice(productKey, deviceKey)
      form.value = { ...data, address: data.address ?? '' }
    }
  },
  { immediate: true }
)

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const addr = form.value.address != null ? String(form.value.address).trim() : ''
    await updateDevice(form.value.productKey, form.value.deviceKey, {
      deviceName: form.value.deviceName,
      address: addr.length > 0 ? addr : null
    })
    ElMessage.success(t('UpdateSuccess'))
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>


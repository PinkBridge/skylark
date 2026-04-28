<template>
  <el-dialog :model-value="visible" :title="$t('ProductCopyTitle')" align-center destroy-on-close :show-close="false" style="max-width: 560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="$t('ProductSourceLabel')">
        <el-input :model-value="`${row?.name || '-'} (${row?.productKey || '-'})`" disabled />
      </el-form-item>
      <el-form-item :label="$t('ProductTargetKeyLabel')" prop="targetProductKey">
        <el-input v-model="form.targetProductKey" :placeholder="$t('ProductTargetKeyPlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('ProductTargetNameLabel')" prop="targetName">
        <el-input v-model="form.targetName" :placeholder="$t('ProductTargetNamePlaceholder')" />
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
import { copyProduct } from '@/views/products/ProductApi'

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const formRef = ref(null)
const form = ref({
  targetProductKey: '',
  targetName: ''
})

const rules = computed(() => ({
  targetProductKey: [
    { required: true, message: t('ProductTargetKeyRequired'), trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]{2,64}$/, message: t('ProductTargetKeyInvalid'), trigger: 'blur' }
  ],
  targetName: [
    { required: true, message: t('ProductTargetNameRequired'), trigger: 'blur' }
  ]
}))

watch(
  () => [props.visible, props.row?.productKey, props.row?.name],
  ([visible]) => {
    if (!visible) return
    form.value = {
      targetProductKey: '',
      targetName: props.row?.name ? `${props.row.name} Copy` : ''
    }
  },
  { immediate: true }
)

const handleCancel = () => {
  formRef.value?.clearValidate?.()
  props.onCancel()
}

const handleSubmit = async () => {
  if (!formRef.value || !props.row?.productKey) return
  try {
    await formRef.value.validate()
    await copyProduct(props.row.productKey, form.value)
    ElMessage.success(t('ProductCopySuccess'))
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>


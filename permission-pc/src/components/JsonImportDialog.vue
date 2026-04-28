<template>
  <el-dialog :model-value="visible" :title="title || t('ImportDialogTitle')" width="520px" @close="onCancel">
    <el-form label-width="110px">
      <el-form-item :label="t('ImportAppCodeLabel')">
        <el-select v-model="localAppCode" style="width: 100%" :placeholder="t('AppCodePlaceholder')">
          <el-option v-for="app in apps" :key="app.clientId" :label="app.clientId" :value="app.clientId" />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('ImportFileLabel')">
        <el-upload
          :auto-upload="false"
          :limit="1"
          :file-list="fileList"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          accept="application/json"
        >
          <el-button type="default">{{ t('ImportFilePlaceholder') }}</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="dryRun">{{ t('ImportDryRunLabel') }}</el-checkbox>
      </el-form-item>

      <el-form-item v-if="result">
        <div style="width: 100%">
          <div>{{ t('ImportResultCreated') }}: {{ result.created }}</div>
          <div>{{ t('ImportResultUpdated') }}: {{ result.updated }}</div>
          <div>{{ t('ImportResultReactivated') }}: {{ result.reactivated }}</div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="onCancel">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ t('ImportSubmitLabel') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '' },
  apps: { type: Array, default: () => [] },
  appCode: { type: String, default: '' },
  importFn: { type: Function, required: true },
  onSuccess: { type: Function, default: () => {} },
  onCancel: { type: Function, default: () => {} }
})

const { t } = useI18n()

const localAppCode = ref('')
const dryRun = ref(false)
const fileList = ref([])
const fileRaw = ref(null)
const submitting = ref(false)
const result = ref(null)

watch(
  () => props.visible,
  (v) => {
    if (v) {
      localAppCode.value = props.appCode || ''
      dryRun.value = false
      fileList.value = []
      fileRaw.value = null
      result.value = null
    }
  }
)

const handleFileChange = (uploadFile, uploadFiles) => {
  fileList.value = uploadFiles.slice(-1)
  fileRaw.value = uploadFile?.raw || null
}

const handleFileRemove = () => {
  fileList.value = []
  fileRaw.value = null
}

const handleSubmit = async () => {
  if (!localAppCode.value) {
    ElMessage.error(t('AppCodeRequired'))
    return
  }
  if (!fileRaw.value) {
    ElMessage.error(t('ImportFilePlaceholder'))
    return
  }
  submitting.value = true
  try {
    const res = await props.importFn(localAppCode.value, fileRaw.value, dryRun.value)
    result.value = res
    ElMessage.success(t('ImportSuccessNotice'))
    props.onSuccess(res)
  } catch (e) {
    console.error(e)
    ElMessage.error(t('ImportFailedNotice'))
  } finally {
    submitting.value = false
  }
}
</script>


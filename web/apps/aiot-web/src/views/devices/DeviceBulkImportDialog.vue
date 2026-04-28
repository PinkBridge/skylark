<template>
  <el-dialog
    :model-value="visible"
    :title="$t('DeviceBulkImportTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 560px"
    @closed="onClosed"
  >
    <p class="hint">{{ $t('DeviceBulkImportHint') }}</p>
    <div class="template-row">
      <el-button type="primary" link :loading="downloadLoading" @click="handleDownloadTemplate">
        {{ $t('DeviceBulkImportDownloadTemplate') }}
      </el-button>
    </div>
    <el-upload
      ref="uploadRef"
      class="import-upload"
      drag
      :auto-upload="false"
      :limit="1"
      accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      :on-change="handleFileChange"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
    >
      <el-icon class="upload-icon"><UploadFilled /></el-icon>
      <div class="el-upload__text">{{ $t('DeviceBulkImportUploadDrag') }}</div>
      <template #tip>
        <div class="el-upload__tip">{{ $t('DeviceBulkImportUploadTip') }}</div>
      </template>
    </el-upload>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onCancel()">{{ $t('CancelButtonText') }}</el-button>
        <el-button type="primary" :loading="importLoading" :disabled="!selectedFile" @click="handleImport">
          {{ $t('DeviceBulkImportSubmit') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { downloadDeviceImportTemplate, importDevicesFromExcel } from '@/views/devices/DeviceApi'

const props = defineProps({
  visible: Boolean,
  onSubmit: Function,
  onCancel: Function
})

const { t } = useI18n()
const uploadRef = ref(null)
const selectedFile = ref(null)
const downloadLoading = ref(false)
const importLoading = ref(false)

watch(
  () => props.visible,
  (vis) => {
    if (!vis) {
      selectedFile.value = null
      uploadRef.value?.clearFiles()
    }
  }
)

const onClosed = () => {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

const handleFileChange = (uploadFile) => {
  const raw = uploadFile.raw
  if (!raw) return
  const name = raw.name || ''
  if (!name.toLowerCase().endsWith('.xlsx')) {
    ElMessage.warning(t('DeviceBulkImportXlsxOnly'))
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    return
  }
  selectedFile.value = raw
}

const handleRemove = () => {
  selectedFile.value = null
}

const handleExceed = () => {
  ElMessage.warning(t('DeviceBulkImportOneFileOnly'))
}

const handleDownloadTemplate = async () => {
  downloadLoading.value = true
  try {
    await downloadDeviceImportTemplate()
    ElMessage.success(t('DeviceBulkImportDownloadOk'))
  } catch (e) {
    ElMessage.error(e?.message || t('RequestFailedNotice'))
  } finally {
    downloadLoading.value = false
  }
}

const formatErrorDetails = (errors) => {
  if (!errors?.length) return ''
  const max = 20
  const slice = errors.slice(0, max)
  const lines = slice.map((e) => t('DeviceBulkImportRowLine', { row: e.rowNumber, msg: e.message || '' }))
  let s = lines.join('\n')
  if (errors.length > max) {
    s += `\n${t('DeviceBulkImportErrorsTruncated', { n: errors.length - max })}`
  }
  return s
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning(t('DeviceBulkImportSelectFile'))
    return
  }
  importLoading.value = true
  try {
    const result = await importDevicesFromExcel(selectedFile.value)
    const ok = result.successCount ?? 0
    const fail = result.failCount ?? 0
    ElMessage.success(t('DeviceBulkImportSummary', { success: ok, fail }))
    if (fail > 0 && result.errors?.length) {
      await ElMessageBox.alert(formatErrorDetails(result.errors), t('DeviceBulkImportErrorsTitle'), {
        confirmButtonText: t('ConfirmButtonText'),
        customClass: 'device-import-error-box'
      })
    }
    if (ok > 0) {
      props.onSubmit?.()
    }
    props.onCancel()
  } catch {
    /* axios interceptor already shows error */
  } finally {
    importLoading.value = false
  }
}
</script>

<style scoped>
.hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
.template-row {
  margin-bottom: 16px;
}
.import-upload {
  width: 100%;
}
.upload-icon {
  font-size: 48px;
  color: var(--el-text-color-placeholder);
}
</style>

<style>
.device-import-error-box .el-message-box__message {
  white-space: pre-wrap;
  max-height: 360px;
  overflow: auto;
}
</style>


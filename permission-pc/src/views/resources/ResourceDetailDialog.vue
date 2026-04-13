<template>
  <el-dialog :model-value="visible" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('IDLabel')">{{ resourceInfo.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('NameLabel')">{{ resourceInfo.name }}</el-descriptions-item>
      <el-descriptions-item :label="t('OriginalNameLabel')">{{ resourceInfo.originalName }}</el-descriptions-item>
      <el-descriptions-item :label="t('FileTypeLabel')">
        <el-tag v-if="resourceInfo.fileType === 'IMAGE'" type="primary">{{ t('FileTypeImage') }}</el-tag>
        <el-tag v-else-if="resourceInfo.fileType === 'DOCUMENT'" type="success">{{ t('FileTypeDocument') }}</el-tag>
        <el-tag v-else-if="resourceInfo.fileType === 'VIDEO'" type="warning">{{ t('FileTypeVideo') }}</el-tag>
        <el-tag v-else-if="resourceInfo.fileType === 'AUDIO'" type="info">{{ t('FileTypeAudio') }}</el-tag>
        <el-tag v-else-if="resourceInfo.fileType === 'OTHER'" type="">{{ t('FileTypeOther') }}</el-tag>
        <span v-else>{{ resourceInfo.fileType || '-' }}</span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('FilePathLabel')">{{ resourceInfo.filePath || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('FileSizeLabel')">
        {{ formatFileSize(resourceInfo.fileSize) }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('MimeTypeLabel')">{{ resourceInfo.mimeType || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('UrlLabel')">
        <a v-if="resourceInfo.url" :href="resourceInfo.url" target="_blank">{{ resourceInfo.url }}</a>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item :label="t('DescriptionLabel')">{{ resourceInfo.description || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('TenantIdLabel')">{{ resourceInfo.tenantId || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ resourceInfo.createTime || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ resourceInfo.updateTime || '-' }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="resourceInfo.fileType === 'IMAGE'" type="default" :loading="previewLoading" @click="handlePreview">
          {{ t('PreviewLabel') }}
        </el-button>
        <el-button type="default" :loading="downloadLoading" @click="handleDownload">{{ t('DownloadLabel') }}</el-button>
        <el-button type="primary" @click="onConfirm">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="previewVisible" :title="t('PreviewLabel')" width="min(90vw, 720px)" align-center destroy-on-close
    append-to-body @closed="revokePreviewUrl">
    <div v-loading="previewImageLoading" class="preview-wrap">
      <el-image v-if="previewObjectUrl" :src="previewObjectUrl" fit="contain" style="width: 100%; max-height: 70vh" />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getResourceById, downloadResource, fetchResourcePreviewBlob } from '@/views/resources/ResourceApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const resourceInfo = ref({})
const downloadLoading = ref(false)
const previewLoading = ref(false)
const previewVisible = ref(false)
const previewObjectUrl = ref('')
const previewImageLoading = ref(false)

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const fetchResourceData = () => {
  if (props.row && props.row.id) {
    getResourceById(props.row.id).then(response => {
      resourceInfo.value = response.resource || response || {}
    }).catch(error => {
      console.error('Failed to get resource information:', error)
      ElMessage.error(error.message || 'Failed to get resource information')
    })
  }
}

const onConfirm = () => {
  props.onConfirm()
}

const revokePreviewUrl = () => {
  if (previewObjectUrl.value) {
    URL.revokeObjectURL(previewObjectUrl.value)
    previewObjectUrl.value = ''
  }
}

const handleDownload = async () => {
  const id = resourceInfo.value?.id
  if (!id) {
    return
  }
  downloadLoading.value = true
  try {
    await downloadResource(id, resourceInfo.value.originalName || resourceInfo.value.name)
  } catch (e) {
    ElMessage.error(e?.message || 'Download failed')
  } finally {
    downloadLoading.value = false
  }
}

const handlePreview = async () => {
  const id = resourceInfo.value?.id
  if (!id || resourceInfo.value.fileType !== 'IMAGE') {
    return
  }
  revokePreviewUrl()
  previewLoading.value = true
  previewVisible.value = true
  previewImageLoading.value = true
  try {
    const blob = await fetchResourcePreviewBlob(id)
    previewObjectUrl.value = URL.createObjectURL(blob)
  } catch (e) {
    previewVisible.value = false
    ElMessage.error(e?.message || 'Preview failed')
  } finally {
    previewImageLoading.value = false
    previewLoading.value = false
  }
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible && rowId) {
      fetchResourceData()
    }
  },
  { immediate: true }
)

watch(
  () => props.visible,
  (v) => {
    if (!v) {
      previewVisible.value = false
      revokePreviewUrl()
    }
  }
)
</script>

<style scoped>
.preview-wrap {
  min-height: 120px;
}
</style>

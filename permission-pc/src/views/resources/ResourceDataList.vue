<template>
  <el-card shadow="always">
    <ResourceSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" min-width="150" />
      <el-table-column prop="originalName" :label="t('OriginalNameLabel')" min-width="150" />
      <el-table-column prop="fileType" :label="t('FileTypeLabel')" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.fileType === 'IMAGE'" type="primary">{{ t('FileTypeImage') }}</el-tag>
          <el-tag v-else-if="row.fileType === 'DOCUMENT'" type="success">{{ t('FileTypeDocument') }}</el-tag>
          <el-tag v-else-if="row.fileType === 'VIDEO'" type="warning">{{ t('FileTypeVideo') }}</el-tag>
          <el-tag v-else-if="row.fileType === 'AUDIO'" type="info">{{ t('FileTypeAudio') }}</el-tag>
          <el-tag v-else-if="row.fileType === 'OTHER'" type="">{{ t('FileTypeOther') }}</el-tag>
          <span v-else>{{ row.fileType || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" :label="t('FileSizeLabel')" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="mimeType" :label="t('MimeTypeLabel')" min-width="120" show-overflow-tooltip />
      <el-table-column prop="url" :label="t('UrlLabel')" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <a v-if="row.url" :href="row.url" target="_blank" style="color: #409eff;">{{ row.url }}</a>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'perm.resources.detail'">
            {{ permLabelName('perm.resources.detail', 'DetailLabel') }}
          </el-button>
          <el-button
            v-if="row.fileType === 'IMAGE'"
            link
            type="primary"
            size="default"
            :loading="previewLoadingId === row.id"
            @click="handleImagePreview(row)"
          >
            {{ t('PreviewLabel') }}
          </el-button>
          <el-button link type="primary" size="default" :loading="downloadLoadingId === row.id" @click="handleDownload(row)">
            {{ t('DownloadLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'perm.resources.delete'">{{ permLabelName('perm.resources.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <ResourceDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getResourcePage, deleteResourceById, downloadResource, fetchResourcePreviewBlob } from '@/views/resources/ResourceApi'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ResourceSearchForm from '@/views/resources/ResourceSearchForm.vue'
import ResourceDetailDialog from '@/views/resources/ResourceDetailDialog.vue'
import { permLabelName } from '@/utils/menuPermLabelNames'

const { t } = useI18n()

// data
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const detailDialogVisible = ref(false)
const detailRow = ref({})
const searchParams = ref({})
const previewVisible = ref(false)
const previewObjectUrl = ref('')
const previewLoading = ref(false)
const previewLoadingId = ref(null)
const downloadLoadingId = ref(null)

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// init data
const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  getResourcePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get resource page:', error)
    ElMessage.error(error.message || 'Failed to get resource page')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  getResourcePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to refresh resource page:', error)
    ElMessage.error(error.message || 'Failed to refresh resource page')
  })
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  currentPage.value = 1 // Reset to first page on new search
  getResourcePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to search resource page:', error)
    ElMessage.error(error.message || 'Failed to search resource page')
  })
}

// view record detail
const handleDetail = (row) => {
  detailRow.value = row
  detailDialogVisible.value = true
}

const handleDetailConfirm = () => {
  detailRow.value = {}
  detailDialogVisible.value = false
}

const revokePreviewUrl = () => {
  if (previewObjectUrl.value) {
    URL.revokeObjectURL(previewObjectUrl.value)
    previewObjectUrl.value = ''
  }
}

const handleImagePreview = async (row) => {
  if (!row?.id || row.fileType !== 'IMAGE') {
    return
  }
  revokePreviewUrl()
  previewLoadingId.value = row.id
  previewLoading.value = true
  previewVisible.value = true
  try {
    const blob = await fetchResourcePreviewBlob(row.id)
    previewObjectUrl.value = URL.createObjectURL(blob)
  } catch (e) {
    previewVisible.value = false
    ElMessage.error(e?.message || t('RequestFailedNotice'))
  } finally {
    previewLoading.value = false
    previewLoadingId.value = null
  }
}

const handleDownload = async (row) => {
  if (!row?.id) {
    return
  }
  downloadLoadingId.value = row.id
  try {
    await downloadResource(row.id, row.originalName || row.name)
  } catch (e) {
    ElMessage.error(e?.message || t('RequestFailedNotice'))
  } finally {
    downloadLoadingId.value = null
  }
}

// delete record
const handleDelete = (id) => {
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning',
  }).then(() => {
    deleteResourceById(id).then(() => {
      initData()
      ElMessage.success('Delete successful!')
    }).catch(error => {
      console.error('Delete failed:', error)
      ElMessage.error(error.message || 'Delete failed')
    })
  }).catch(() => {
    // User cancelled, do nothing
  })
}

// change page size
const handleSizeChange = (size) => {
  pageSize.value = size
  getResourcePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to change page size:', error)
    ElMessage.error(error.message || 'Failed to change page size')
  })
}

// change current page
const handleCurrentChange = (page) => {
  currentPage.value = page
  getResourcePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to change current page:', error)
    ElMessage.error(error.message || 'Failed to change current page')
  })
}

// mounted
onMounted(() => {
  initData()
})
</script>

<style scoped>
.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.buttons-block {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

.preview-wrap {
  min-height: 120px;
}
</style>


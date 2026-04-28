<template>
  <el-card shadow="always">
    <ProductSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="createDialogVisible = true">{{ t('NewButtonLabel') }}</el-button>
        <el-button :icon="Refresh" @click="loadData">{{ t('RefreshButtonLabel') }}</el-button>
      </div>
      <el-radio-group v-model="viewMode" size="default" class="view-switch">
        <el-radio-button label="card">
          <el-icon><Grid /></el-icon>
          <span>{{ t('ProductCardViewLabel') }}</span>
        </el-radio-button>
        <el-radio-button label="table">
          <el-icon><Operation /></el-icon>
          <span>{{ t('ProductTableViewLabel') }}</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="viewMode === 'card' && tableData.length" class="product-card-grid">
      <el-card v-for="item in tableData" :key="item.productKey" class="product-card" shadow="hover">
        <div class="product-card-cover">
          <el-image :src="item.coverImageUrl" fit="contain" class="cover-image">
            <template #error>
              <div class="cover-placeholder">{{ t('ProductNoCover') }}</div>
            </template>
          </el-image>
        </div>
        <div class="product-card-body">
          <div class="product-card-header">
            <div class="product-card-title">{{ item.name || '-' }}</div>
            <el-tag size="small" :type="item.status === 'enabled' ? 'success' : 'info'">
              {{ item.status === 'enabled' ? t('Enabled') : t('Disabled') }}
            </el-tag>
          </div>
          <div class="product-card-key">{{ item.productKey || '-' }}</div>
          <div class="product-card-meta">
            <span class="meta-chip">{{ item.protocolType || '-' }}</span>
            <span class="meta-chip">{{ item.deviceType || '-' }}</span>
            <span class="meta-chip">{{ t('ProductDeviceCountText', { count: item.deviceCount || 0 }) }}</span>
          </div>
          <div class="product-card-desc">{{ item.description || '-' }}</div>
          <div class="product-card-actions">
            <el-button link type="primary" @click="handleDetail(item)">{{ t('DetailLabel') }}</el-button>
            <el-button link type="primary" @click="handleEdit(item)">{{ t('EditLabel') }}</el-button>
            <el-button link type="primary" @click="handleCopy(item)">{{ t('CopyLabel') }}</el-button>
            <el-button link type="primary" @click="toggleStatus(item)">
              {{ item.status === 'enabled' ? t('Disable') : t('Enable') }}
            </el-button>
            <el-button link type="primary" @click="handleDelete(item.productKey)">{{ t('DeleteLabel') }}</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-else-if="viewMode === 'card'" :description="t('PlaceholderReady')" />

    <el-table v-else :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column :label="t('ProductThumbnailLabel')" width="110" align="center">
        <template #default="{ row }">
          <el-image :src="row.thumbnailUrl || row.coverImageUrl" fit="cover" class="table-image">
            <template #error>
              <div class="table-image-placeholder">{{ t('ProductNoImage') }}</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="productKey" :label="t('ProductKeyLabel')" min-width="180" />
      <el-table-column prop="name" :label="t('NameLabel')" min-width="180" />
      <el-table-column prop="description" :label="t('DescriptionLabel')" min-width="220" />
      <el-table-column prop="protocolType" :label="t('ProductProtocolTypeLabel')" min-width="180" />
      <el-table-column prop="deviceType" :label="t('DeviceTypeLabel')" min-width="160" />
      <el-table-column prop="status" :label="t('StatusLabel')" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'danger'">
            {{ row.status === 'enabled' ? t('Enabled') : t('Disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('OperationsLabel')" min-width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">{{ t('DetailLabel') }}</el-button>
          <el-button link type="primary" @click="handleEdit(row)">{{ t('EditLabel') }}</el-button>
          <el-button link type="primary" @click="handleCopy(row)">{{ t('CopyLabel') }}</el-button>
          <el-button link type="primary" @click="toggleStatus(row)">
            {{ row.status === 'enabled' ? t('Disable') : t('Enable') }}
          </el-button>
          <el-button link type="primary" @click="handleDelete(row.productKey)">{{ t('DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="loadData"
        @size-change="handlePageSizeChange"
      />
    </div>

    <ProductCreateDialog
      :visible="createDialogVisible"
      :on-submit="handleCreateSubmit"
      :on-cancel="() => { createDialogVisible = false }"
    />
    <ProductEditDialog
      v-if="editRow.productKey"
      :visible="editDialogVisible"
      :row="editRow"
      :on-submit="handleEditSubmit"
      :on-cancel="handleEditCancel"
    />
    <ProductCopyDialog
      v-if="copyRow.productKey"
      :visible="copyDialogVisible"
      :row="copyRow"
      :on-submit="handleCopySubmit"
      :on-cancel="handleCopyCancel"
    />
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Grid, Operation } from '@element-plus/icons-vue'
import ProductSearchForm from '@/views/products/ProductSearchForm.vue'
import ProductCreateDialog from '@/views/products/ProductCreateDialog.vue'
import ProductEditDialog from '@/views/products/ProductEditDialog.vue'
import ProductCopyDialog from '@/views/products/ProductCopyDialog.vue'
import { deleteProduct, disableProduct, enableProduct, getProductList } from '@/views/products/ProductApi'

const { t } = useI18n()
const router = useRouter()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const viewMode = ref('card')
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const copyDialogVisible = ref(false)
const editRow = ref({})
const copyRow = ref({})
const searchParams = ref({
  productKey: '',
  name: '',
  status: ''
})

const loadData = async () => {
  try {
    const data = await getProductList({
      ...searchParams.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    tableData.value = data.records || []
    total.value = data.total || 0
    pageNum.value = data.pageNum || pageNum.value
    pageSize.value = data.pageSize || pageSize.value
  } catch (error) {
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

const handleSearch = (params) => {
  searchParams.value = params
  pageNum.value = 1
  loadData()
}

const handleReset = (params) => {
  searchParams.value = params
  pageNum.value = 1
  loadData()
}

const handlePageSizeChange = (value) => {
  pageSize.value = value
  pageNum.value = 1
  loadData()
}

const handleCreateSubmit = () => {
  createDialogVisible.value = false
  pageNum.value = 1
  loadData()
}

const handleEdit = (row) => {
  editRow.value = row
  editDialogVisible.value = true
}

const handleCopy = (row) => {
  copyRow.value = row
  copyDialogVisible.value = true
}

const handleDetail = (row) => {
  router.push(`/iot/products/${row.productKey}`)
}

const handleEditSubmit = () => {
  editDialogVisible.value = false
  editRow.value = {}
  loadData()
}

const handleEditCancel = () => {
  editDialogVisible.value = false
  editRow.value = {}
}

const handleCopySubmit = () => {
  copyDialogVisible.value = false
  copyRow.value = {}
  pageNum.value = 1
  loadData()
}

const handleCopyCancel = () => {
  copyDialogVisible.value = false
  copyRow.value = {}
}

const handleDelete = (productKey) => {
  ElMessageBox.confirm(t('ProductDeleteCascadeConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning'
  }).then(async () => {
    await deleteProduct(productKey)
    ElMessage.success(t('DeleteSuccess'))
    if (tableData.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    loadData()
  }).catch(() => {})
}

const toggleStatus = async (row) => {
  try {
    if (row.status === 'enabled') {
      await disableProduct(row.productKey)
    } else {
      await enableProduct(row.productKey)
    }
    loadData()
  } catch (error) {
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-switch :deep(.el-radio-button__inner) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.product-card-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.product-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.product-card-cover {
  height: 168px;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image :deep(img) {
  object-fit: contain;
  object-position: center center;
  background: transparent;
}

.cover-placeholder,
.table-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  background: linear-gradient(135deg, #f7f9fc 0%, #e9eef7 100%);
  font-size: 13px;
}

.product-card-body {
  padding-top: 12px;
}

.product-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 6px;
}

.product-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.product-card-key {
  margin-bottom: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.4;
}

.product-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 12px;
  line-height: 1;
}

.product-card-desc {
  min-height: 36px;
  margin-bottom: 12px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--el-text-color-regular);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 10px;
}

.table-image {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1600px) {
  .product-card-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1280px) {
  .product-card-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .product-card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .product-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>


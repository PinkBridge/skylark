<template>
  <el-card shadow="always">
    <DeviceSearchForm
      :products="products"
      :product-key="selectedProductKey"
      :search="handleSearch"
      :reset="handleReset"
      :product-change="handleProductChange"
    />
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="createDialogVisible = true">{{ t('NewButtonLabel') }}</el-button>
        <el-button :icon="Upload" @click="bulkImportVisible = true">{{ t('DeviceBulkImportButton') }}</el-button>
        <el-button :icon="Refresh" @click="() => loadDevices(true)">{{ t('RefreshButtonLabel') }}</el-button>
      </div>
      <el-radio-group v-model="viewMode" size="default" class="view-switch">
        <el-radio-button label="card">
          <el-icon><Grid /></el-icon>
          <span>{{ t('DeviceCardViewLabel') }}</span>
        </el-radio-button>
        <el-radio-button label="table">
          <el-icon><Operation /></el-icon>
          <span>{{ t('DeviceTableViewLabel') }}</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="viewMode === 'card' && pagedData.length" class="device-card-grid">
      <el-card v-for="row in pagedData" :key="`${row.productKey}-${row.deviceKey}`" class="device-card" shadow="hover">
        <div class="device-card-header">
          <div class="device-card-title">
            <el-image
              :src="getProductThumb(row.productKey)"
              fit="cover"
              class="product-thumb product-thumb-card"
            >
              <template #error>
                <div class="product-thumb-placeholder">{{ t('ProductNoImage') }}</div>
              </template>
            </el-image>
            <div class="device-title-texts">
              <div class="device-title-main">{{ row.deviceName || '-' }}</div>
              <div class="device-title-sub">{{ row.deviceKey || '-' }}</div>
            </div>
          </div>
          <div class="device-card-status-tags">
            <el-tag size="small" :type="row.connectStatus === 'connected' ? 'success' : 'info'">
              {{ getConnectTagText(row) }}
            </el-tag>
            <el-tag size="small" :type="row.status === 'enabled' ? 'success' : 'info'">
              {{ row.status === 'enabled' ? t('Enabled') : t('Disabled') }}
            </el-tag>
          </div>
        </div>
        <div class="device-card-meta">
          <div class="device-card-product-name">{{ getProductName(row.productKey) }}</div>
          <div v-if="row.address" class="device-card-address">{{ row.address }}</div>
          <div class="meta-chips">
            <span class="meta-chip">{{ row.deviceType || '-' }}</span>
            <span class="meta-chip">{{ row.protocolType || '-' }}</span>
          </div>
        </div>
        <div class="device-card-actions">
          <el-button link type="primary" @click="handleDetail(row)">{{ t('DetailLabel') }}</el-button>
          <el-button link type="primary" @click="handleEdit(row)">{{ t('EditLabel') }}</el-button>
          <el-button link type="primary" @click="toggleStatus(row)">
            {{ row.status === 'enabled' ? t('Disable') : t('Enable') }}
          </el-button>
          <el-button link type="primary" @click="handleDelete(row)">{{ t('DeleteLabel') }}</el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-else-if="viewMode === 'card'" :description="t('DeviceRecordsEmpty')" />

    <el-table v-else :data="pagedData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column prop="deviceKey" :label="t('DeviceKeyLabel')" min-width="200" show-overflow-tooltip />
      <el-table-column :label="t('DeviceNameLabel')" min-width="220">
        <template #default="{ row }">
          <div class="device-name-cell">
            <el-image
              :src="getProductThumb(row.productKey)"
              fit="cover"
              class="product-thumb product-thumb-small"
            >
              <template #error>
                <div class="product-thumb-placeholder product-thumb-placeholder-small">{{ t('ProductNoImage') }}</div>
              </template>
            </el-image>
            <div class="device-name-cell-text">
              <div class="device-name-main">{{ row.deviceName || '-' }}</div>
              <div class="device-name-sub">{{ getProductName(row.productKey) }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="deviceType" :label="t('DeviceTypeLabel')" min-width="160" />
      <el-table-column prop="protocolType" :label="t('ProtocolTypeLabel')" min-width="140" />
      <el-table-column prop="connectStatus" :label="t('ConnectionStatusLabel')" min-width="140">
        <template #default="{ row }">
          <el-tag size="small" :type="row.connectStatus === 'connected' ? 'success' : 'info'">
            {{ getConnectTagText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="t('StatusLabel')" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'danger'">
            {{ row.status === 'enabled' ? t('Enabled') : t('Disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('OperationsLabel')" min-width="320" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">{{ t('DetailLabel') }}</el-button>
          <el-button link type="primary" @click="handleEdit(row)">{{ t('EditLabel') }}</el-button>
          <el-button link type="primary" @click="toggleStatus(row)">
            {{ row.status === 'enabled' ? t('Disable') : t('Enable') }}
          </el-button>
          <el-button link type="primary" @click="handleDelete(row)">{{ t('DeleteLabel') }}</el-button>
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
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </div>

    <DeviceBulkImportDialog
      :visible="bulkImportVisible"
      :on-submit="handleBulkImportSubmit"
      :on-cancel="() => { bulkImportVisible = false }"
    />
    <DeviceCreateDialog
      :visible="createDialogVisible"
      :products="products"
      :product-key="selectedProductKey"
      :on-submit="handleCreateSubmit"
      :on-cancel="() => { createDialogVisible = false }"
    />
    <DeviceEditDialog
      v-if="editRow.deviceKey"
      :visible="editDialogVisible"
      :row="editRow"
      :on-submit="handleEditSubmit"
      :on-cancel="handleEditCancel"
    />
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Upload, Grid, Operation } from '@element-plus/icons-vue'
import { getProductList } from '@/views/products/ProductApi'
import DeviceSearchForm from '@/views/devices/DeviceSearchForm.vue'
import DeviceBulkImportDialog from '@/views/devices/DeviceBulkImportDialog.vue'
import DeviceCreateDialog from '@/views/devices/DeviceCreateDialog.vue'
import DeviceEditDialog from '@/views/devices/DeviceEditDialog.vue'
import {
  deleteDevice,
  disableDevice,
  enableDevice,
  getAllDevices,
  getDeviceList
} from '@/views/devices/DeviceApi'

const { t } = useI18n()
const router = useRouter()
const products = ref([])
const selectedProductKey = ref('')
const viewMode = ref('card')
const tableData = ref([])
const rawData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const bulkImportVisible = ref(false)
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editRow = ref({})
const isLoadingDevices = ref(false)
const lastLoadedProductKey = ref(null)
const searchParams = ref({
  productKey: '',
  deviceKey: '',
  deviceName: '',
  status: ''
})

const loadProducts = async () => {
  const data = await getProductList({
    pageNum: 1,
    pageSize: 100
  })
  products.value = Array.isArray(data) ? data : (data.records || [])
}

const applyFilter = () => {
  const { deviceKey, deviceName, status } = searchParams.value
  tableData.value = rawData.value.filter((item) => {
    const matchDeviceKey = !deviceKey || item.deviceKey?.toLowerCase().includes(deviceKey.toLowerCase())
    const matchDeviceName = !deviceName || item.deviceName?.toLowerCase().includes(deviceName.toLowerCase())
    const matchStatus = !status || item.status === status
    return matchDeviceKey && matchDeviceName && matchStatus
  })
  total.value = tableData.value.length
  if (pageNum.value > Math.ceil(total.value / pageSize.value)) {
    pageNum.value = 1
  }
}

const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

const productMap = computed(() => {
  const map = new Map()
  products.value.forEach((p) => {
    if (p?.productKey) map.set(p.productKey, p)
  })
  return map
})

const getProductName = (productKey) => {
  const p = productMap.value.get(productKey)
  return p?.name || '-'
}

const getProductThumb = (productKey) => {
  const p = productMap.value.get(productKey)
  return p?.thumbnailUrl || p?.coverImageUrl || ''
}

const formatLocalTime = (value) => {
  if (!value) return ''
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleString()
}

const getConnectTimeText = (row) => {
  if (!row) return ''
  if (row.connectStatus === 'connected') return formatLocalTime(row.lastConnectedAt)
  return formatLocalTime(row.lastDisconnectedAt)
}

const getConnectTagText = (row) => {
  if (!row) return ''
  const statusText = row.connectStatus === 'connected' ? t('Connected') : t('Disconnected')
  const timeText = getConnectTimeText(row)
  return timeText ? `${statusText} ${timeText}` : statusText
}

const handlePageChange = (value) => {
  pageNum.value = value
}

const handlePageSizeChange = (value) => {
  pageSize.value = value
  pageNum.value = 1
}

const loadDevices = async (force = false) => {
  if (isLoadingDevices.value) return
  // 如果 productKey 没变化且 rawData 已经在内存里，就不再重复请求。
  const currentKey = selectedProductKey.value || ''
  if (!force && lastLoadedProductKey.value === currentKey && rawData.value?.length) {
    applyFilter()
    return
  }
  try {
    isLoadingDevices.value = true
    // 未选择产品时：请求该租户下的全部设备（单次请求）。
    if (!selectedProductKey.value) {
      rawData.value = await getAllDevices()
      lastLoadedProductKey.value = currentKey
      applyFilter()
      return
    }

    rawData.value = await getDeviceList(selectedProductKey.value)
    lastLoadedProductKey.value = currentKey
    applyFilter()
  } catch (error) {
    ElMessage.error(error.message || t('RequestFailedNotice'))
  } finally {
    isLoadingDevices.value = false
  }
}

const handleSearch = (params) => {
  searchParams.value = params
  selectedProductKey.value = params.productKey
  pageNum.value = 1
  // Query 按钮：强制刷新一次后端数据（符合“点击就请求后台”的预期）
  loadDevices(true)
}

const handleReset = (params) => {
  searchParams.value = params
  selectedProductKey.value = params.productKey
  pageNum.value = 1
  // Reset 按钮：强制刷新一次后端数据
  loadDevices(true)
}

const handleProductChange = (productKey) => {
  if (productKey === selectedProductKey.value) return
  selectedProductKey.value = productKey
  searchParams.value.productKey = productKey
  pageNum.value = 1
  loadDevices(true)
}

const handleCreateSubmit = () => {
  createDialogVisible.value = false
  loadDevices(true)
}

const handleBulkImportSubmit = () => {
  loadDevices(true)
}

const handleEdit = (row) => {
  editRow.value = row
  editDialogVisible.value = true
}

const handleDetail = (row) => {
  const productKey = row.productKey || selectedProductKey.value
  if (!productKey || !row.deviceKey) return
  router.push(`/iot/devices/${productKey}/${row.deviceKey}`)
}

const handleEditSubmit = () => {
  editDialogVisible.value = false
  editRow.value = {}
  loadDevices(true)
}

const handleEditCancel = () => {
  editDialogVisible.value = false
  editRow.value = {}
}

const handleDelete = (row) => {
  const productKey = row.productKey || selectedProductKey.value
  if (!productKey) return
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning'
  }).then(async () => {
    await deleteDevice(productKey, row.deviceKey)
    ElMessage.success(t('DeleteSuccess'))
    loadDevices(true)
  }).catch(() => {})
}

const toggleStatus = async (row) => {
  const productKey = row.productKey || selectedProductKey.value
  if (!productKey) return
  try {
    if (row.status === 'enabled') {
      await disableDevice(productKey, row.deviceKey)
    } else {
      await enableDevice(productKey, row.deviceKey)
    }
    loadDevices(true)
  } catch (error) {
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

onMounted(async () => {
  await loadProducts()
  // 默认不选中产品：直接拉取租户下全部设备。
  await loadDevices()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.device-card-grid {
  display: grid;
  /* Fully responsive: keep each card >= 260px; column count adapts automatically */
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

/* 大屏最多 5 列 */
@media (min-width: 1400px) {
  .device-card-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

.device-card {
  border-radius: 10px;
}

.device-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
  padding: 6px 2px;
  min-height: 58px;
  flex-wrap: wrap;
}

.device-card-status-tags {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  flex-wrap: wrap;
}

.device-card-title {
  display: inline-flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  word-break: break-word;
  min-width: 0;
}

.device-title-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.device-title-main {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1.25;
  word-break: break-word;
}

.device-title-sub {
  font-size: 12px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
  line-height: 1.25;
  word-break: break-word;
}

.product-thumb {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-light);
  overflow: hidden;
  flex: 0 0 auto;
}

/* 设备卡片：缩略图无边框 */
.product-thumb-card {
  border: none;
}

.product-thumb-small {
  width: 28px;
  height: 28px;
  border-radius: 6px;
}

.product-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 10px;
}

.product-thumb-placeholder-small {
  font-size: 9px;
}

.device-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.device-name-cell-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.device-name-main {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.device-name-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
}

.device-card-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.device-card-product-name {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.35;
  word-break: break-word;
}

.device-card-address {
  font-size: 12px;
  color: var(--el-text-color-regular);
  line-height: 1.4;
  word-break: break-word;
}

.meta-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.meta-chip {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.device-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
}
</style>


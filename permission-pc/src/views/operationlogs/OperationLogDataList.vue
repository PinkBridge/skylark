<template>
  <el-card shadow="always">
    <OperationLogSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="username" :label="t('UsernameLabel')" min-width="120" />
      <el-table-column prop="httpMethod" :label="t('HttpMethodLabel')" width="90" />
      <el-table-column prop="httpStatus" :label="t('HttpStatusLabel')" width="100" />
      <el-table-column prop="requestUri" :label="t('RequestUriLabel')" min-width="240" show-overflow-tooltip />
      <el-table-column prop="durationMs" :label="t('DurationMsLabel')" width="110" />
      <el-table-column prop="clientIp" :label="t('LoginIpLabel')" min-width="130" />
      <el-table-column prop="createdAt" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)"
            v-permission="'logger.operation.detail'">
            {{ permLabelName('logger.operation.detail', 'DetailLabel') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <OperationLogDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOperationLogPage } from '@/views/operationlogs/OperationLogApi'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import OperationLogSearchForm from '@/views/operationlogs/OperationLogSearchForm.vue'
import OperationLogDetailDialog from '@/views/operationlogs/OperationLogDetailDialog.vue'
import { permLabelName } from '@/utils/menuPermLabelNames'

const { t } = useI18n()

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const detailDialogVisible = ref(false)
const detailRow = ref({})
const searchParams = ref({})

const normalizeParams = (params) => {
  const p = { ...params }
  if (p.createdTime && Array.isArray(p.createdTime) && p.createdTime.length === 2) {
    p.createdTimeStart = p.createdTime[0] ? new Date(p.createdTime[0]).toISOString() : null
    p.createdTimeEnd = p.createdTime[1] ? new Date(p.createdTime[1]).toISOString() : null
    delete p.createdTime
  }
  if (p.httpStatus !== '' && p.httpStatus != null) {
    const n = parseInt(String(p.httpStatus), 10)
    p.httpStatus = Number.isNaN(n) ? undefined : n
  } else {
    delete p.httpStatus
  }
  return p
}

const loadPage = (page, size, params) => {
  getOperationLogPage(page, size, params).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('operation log page failed:', error)
    ElMessage.error(error.message || 'operation log page failed')
  })
}

const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  loadPage(currentPage.value, pageSize.value, searchParams.value)
}

const handleReset = (params) => {
  searchParams.value = normalizeParams(params)
  currentPage.value = 1
  loadPage(currentPage.value, pageSize.value, searchParams.value)
}

const handleRefresh = () => {
  loadPage(currentPage.value, pageSize.value, searchParams.value)
}

const handleSearch = (params) => {
  searchParams.value = normalizeParams(params)
  currentPage.value = 1
  loadPage(currentPage.value, pageSize.value, searchParams.value)
}

const handleDetail = (row) => {
  detailRow.value = row
  detailDialogVisible.value = true
}

const handleDetailConfirm = () => {
  detailRow.value = {}
  detailDialogVisible.value = false
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadPage(currentPage.value, pageSize.value, searchParams.value)
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadPage(currentPage.value, pageSize.value, searchParams.value)
}

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
</style>

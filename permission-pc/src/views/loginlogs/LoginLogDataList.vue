<template>
  <el-card shadow="always">
    <LoginLogSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="username" :label="t('UsernameLabel')" min-width="120" />
      <el-table-column prop="loginIp" :label="t('LoginIpLabel')" min-width="140" />
      <el-table-column prop="clientId" :label="t('ClientIdLabel')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="loginStatus" :label="t('LoginStatusLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.loginStatus === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.loginStatus === 'SUCCESS' ? t('Success') : t('Failure') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="loginTime" :label="t('LoginTimeLabel')" width="180" />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'logger.login.detail'">
            {{ permLabelName('logger.login.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'logger.login.delete'">{{ permLabelName('logger.login.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <LoginLogDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLoginLogPage, deleteLoginLogById } from '@/views/loginlogs/LoginLogApi'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import LoginLogSearchForm from '@/views/loginlogs/LoginLogSearchForm.vue'
import LoginLogDetailDialog from '@/views/loginlogs/LoginLogDetailDialog.vue'
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

// init data
const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  getLoginLogPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get login log page:', error)
    ElMessage.error(error.message || 'Failed to get login log page')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  getLoginLogPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to refresh login log page:', error)
    ElMessage.error(error.message || 'Failed to refresh login log page')
  })
}

// filter data
const handleSearch = (params) => {
  // Process date range
  const processedParams = { ...params }
  if (params.loginTime && Array.isArray(params.loginTime) && params.loginTime.length === 2) {
    processedParams.loginTimeStart = params.loginTime[0] ? new Date(params.loginTime[0]).toISOString() : null
    processedParams.loginTimeEnd = params.loginTime[1] ? new Date(params.loginTime[1]).toISOString() : null
    delete processedParams.loginTime
  }
  searchParams.value = processedParams
  currentPage.value = 1 // Reset to first page on new search
  getLoginLogPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to search login log page:', error)
    ElMessage.error(error.message || 'Failed to search login log page')
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

// delete record
const handleDelete = (id) => {
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning',
  }).then(() => {
    deleteLoginLogById(id).then(() => {
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
  getLoginLogPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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
  getLoginLogPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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
</style>


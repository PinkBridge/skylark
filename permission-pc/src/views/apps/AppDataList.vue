<template>
  <el-card shadow="always">
    <AppSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" 
      v-permission="'system.apps.new'"
      @click="handleCreate">{{ permLabelName('system.apps.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="clientId" :label="t('ClientIdLabel')" min-width="80" />
      <el-table-column prop="autoapprove" :label="t('AutoapproveLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.autoapprove == true ? 'success' : 'danger'">
            {{ row.autoapprove == true ? t('Yes') : t('No') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="authorizedGrantTypes" :label="t('AuthorizedGrantTypesLabel')" min-width="200">
        <template #default="{ row }">
          {{ formatArray(row.authorizedGrantTypes) }}
        </template>
      </el-table-column>
      <el-table-column prop="webServerRedirectUri" :label="t('WebServerRedirectUriLabel')" min-width="200" show-overflow-tooltip />
      <el-table-column prop="scope" :label="t('ScopeLabel')" min-width="120">
        <template #default="{ row }">
          {{ formatArray(row.scope) }}
        </template>
      </el-table-column>
      <el-table-column prop="accessTokenValidity" :label="t('AccessTokenValidityLabel')" width="120" />
      <el-table-column prop="refreshTokenValidity" :label="t('RefreshTokenValidityLabel')" width="120" />

      <el-table-column prop="authorities" :label="t('AuthoritiesLabel')" min-width="120">
        <template #default="{ row }">
          {{ formatArray(row.authorities) }}
        </template>
      </el-table-column>
      <el-table-column prop="resourceIds" :label="t('ResourceIdsLabel')" min-width="120">
        <template #default="{ row }">
          {{ formatArray(row.resourceIds) }}
        </template>
      </el-table-column>
      <el-table-column :label="t('OperationsLabel')" min-width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'system.apps.detail'">
            {{ permLabelName('system.apps.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'system.apps.edit'">{{ permLabelName('system.apps.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.clientId)" v-permission="'system.apps.delete'">{{ permLabelName('system.apps.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <AppDetailDialog v-if="detailRow && detailRow.clientId" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <AppCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <AppEditDialog v-if="editRow && editRow.clientId" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAppPage, deleteAppByClientId } from '@/views/apps/AppApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import AppSearchForm from '@/views/apps/AppSearchForm.vue'
import AppDetailDialog from '@/views/apps/AppDetailDialog.vue'
import AppCreateDialog from '@/views/apps/AppCreateDialog.vue'
import AppEditDialog from '@/views/apps/AppEditDialog.vue'
import { permLabelName } from '@/utils/menuPermLabelNames'

const { t } = useI18n()

// data
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailRow = ref({})
const editRow = ref({})
const searchParams = ref({})

const formatArray = (value) => {
  if (!value) return '-'
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'string') {
    return value
  }
  return '-'
}

// init data
const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  getAppPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get app page:', error)
    ElMessage.error(error.message || 'Failed to get app page')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  getAppPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to refresh app page:', error)
    ElMessage.error(error.message || 'Failed to refresh app page')
  })
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  currentPage.value = 1 // Reset to first page on new search
  getAppPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to search app page:', error)
    ElMessage.error(error.message || 'Failed to search app page')
  })
}

// new record
const handleCreate = () => {
  createDialogVisible.value = true
}

const handleCreateSubmit = () => {
  initData()
  createDialogVisible.value = false
}

const handleCreateCancel = () => {
  createDialogVisible.value = false
}

// edit record
const handleEdit = (row) => {
  editRow.value = row
  editDialogVisible.value = true
}

const handleEditSubmit = () => {
  handleRefresh()
  editDialogVisible.value = false
}

const handleEditCancel = () => {
  editRow.value = {}
  editDialogVisible.value = false
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
const handleDelete = (clientId) => {
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning',
  }).then(() => {
    deleteAppByClientId(clientId).then(() => {
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
  getAppPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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
  getAppPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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


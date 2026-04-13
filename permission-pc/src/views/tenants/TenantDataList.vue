<template>
  <el-card shadow="always">
    <TenantSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" v-permission="'perm.tenants.new'" size="default" :icon="Plus" @click="handleCreate">{{ permLabelName('perm.tenants.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="logo" :label="t('LogoLabel')" width="120">
        <template #default="{ row }">
          <el-image v-if="row.logo" style="width: 40px; height: 40px" :src="row.logo" fit="contain" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="t('NameLabel')" min-width="150" />
      <el-table-column prop="code" :label="t('CodeLabel')" min-width="120" />
      <el-table-column prop="systemName" :label="t('SystemNameLabel')" min-width="150" />
      <el-table-column prop="status" :label="t('StatusLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? t('Active') : t('Disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="domain" :label="t('DomainLabel')" min-width="180" show-overflow-tooltip />
      <el-table-column prop="contactName" :label="t('ContactNameLabel')" min-width="140" />
      <el-table-column prop="contactPhone" :label="t('ContactPhoneLabel')" min-width="140" />
      <el-table-column prop="contactEmail" :label="t('ContactEmailLabel')" min-width="180" />
      <el-table-column prop="expireTime" :label="t('ExpireTimeLabel')" width="180" />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="320" fixed="right" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <div class="op-actions">
            <el-button link type="primary" v-permission="'perm.tenants.detail'" size="default" @click="handleDetail(row)">
              {{ permLabelName('perm.tenants.detail', 'DetailLabel') }}
            </el-button>
            <el-button link type="primary" v-permission="'perm.tenants.edit'" size="default" @click="handleEdit(row)">{{ permLabelName('perm.tenants.edit', 'EditLabel') }}</el-button>
            <el-button link type="primary" v-permission="'perm.tenants.edit'" size="default" @click="handleCreateTenantAdmin(row)">
              {{ t('NewTenantAdminButtonLabel') }}
            </el-button>
            <el-button link type="primary" v-permission="'perm.tenants.delete'" size="default" @click="handleDelete(row.id)">{{ permLabelName('perm.tenants.delete', 'DeleteLabel') }}</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <TenantDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <TenantCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <TenantEditDialog v-if="editRow && editRow.id" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
    <TenantAdminCreateDialog :visible="tenantAdminDialogVisible" :tenantId="selectedTenantId"
      :onSubmit="handleCreateTenantAdminSubmit" :onCancel="handleCreateTenantAdminCancel" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getTenantPage, deleteTenantById } from '@/views/tenants/TenantApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import TenantSearchForm from '@/views/tenants/TenantSearchForm.vue'
import TenantDetailDialog from '@/views/tenants/TenantDetailDialog.vue'
import TenantCreateDialog from '@/views/tenants/TenantCreateDialog.vue'
import TenantEditDialog from '@/views/tenants/TenantEditDialog.vue'
import TenantAdminCreateDialog from '@/views/tenants/TenantAdminCreateDialog.vue'
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
const tenantAdminDialogVisible = ref(false)
const detailRow = ref({})
const editRow = ref({})
const selectedTenantId = ref(null)
const searchParams = ref({})

// init data
const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  getTenantPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get tenant page:', error)
    ElMessage.error(error.message || 'Failed to get tenant page')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  getTenantPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to refresh tenant page:', error)
    ElMessage.error(error.message || 'Failed to refresh tenant page')
  })
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  currentPage.value = 1 // Reset to first page on new search
  getTenantPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to search tenant page:', error)
    ElMessage.error(error.message || 'Failed to search tenant page')
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

const handleCreateTenantAdmin = (row) => {
  selectedTenantId.value = row?.id || null
  tenantAdminDialogVisible.value = true
}

const handleCreateTenantAdminSubmit = () => {
  tenantAdminDialogVisible.value = false
  selectedTenantId.value = null
}

const handleCreateTenantAdminCancel = () => {
  tenantAdminDialogVisible.value = false
  selectedTenantId.value = null
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
    deleteTenantById(id).then(() => {
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
  getTenantPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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
  getTenantPage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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

.op-actions {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
</style>



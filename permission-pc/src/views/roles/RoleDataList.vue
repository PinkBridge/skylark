<template>
  <el-card shadow="always">
    <RoleSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" 
      v-permission="'perm.roles.new'"
      @click="handleCreate">{{ permLabelName('perm.roles.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" min-width="120" />
      <el-table-column prop="tenantName" :label="t('TenantNameLabel')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="remark" :label="t('RemarkLabel')" />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" />
      <el-table-column :label="t('OperationsLabel')" min-width="120" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'perm.roles.detail'">
            {{ permLabelName('perm.roles.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'perm.roles.edit'">{{ permLabelName('perm.roles.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'perm.roles.delete'">{{ permLabelName('perm.roles.delete', 'DeleteLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleApiSelect(row)" v-permission="'perm.roles.api'">
            {{ permLabelName('perm.roles.api', 'APIsLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleMenuSelect(row)"
            v-permission="'perm.roles.api'">
            {{ t('MenusLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleDataDomainSelect(row)"
            v-permission="'perm.roles.api'">
            {{ t('DataDomainsLabel') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <RoleDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <RoleCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <RoleEditDialog v-if="editRow && editRow.id" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
    <ApiSelectTableDialog v-if="apiSelectRow && apiSelectRow.id" :visible="apiSelectTableVisible" :row="apiSelectRow"
      :onConfirm="handleApiSelectConfirm" :onCancel="handleApiSelectCancel" />
    <MenuSelectTableDialog v-if="menuSelectRow && menuSelectRow.id" :visible="menuSelectTableVisible"
      :row="menuSelectRow" :onConfirm="handleMenuSelectConfirm" :onCancel="handleMenuSelectCancel" />
    <DataDomainSelectTableDialog v-if="dataDomainSelectRow && dataDomainSelectRow.id"
      :visible="dataDomainSelectTableVisible" :row="dataDomainSelectRow"
      :onConfirm="handleDataDomainSelectConfirm" :onCancel="handleDataDomainSelectCancel" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getRolePage, deleteRoleById } from '@/views/roles/RoleApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RoleSearchForm from '@/views/roles/RoleSearchForm.vue'
import RoleDetailDialog from '@/views/roles/RoleDetailDialog.vue'
import RoleCreateDialog from '@/views/roles/RoleCreateDialog.vue'
import RoleEditDialog from '@/views/roles/RoleEditDialog.vue'
import ApiSelectTableDialog from '@/views/roles/ApiSelectTableDialog.vue'
import MenuSelectTableDialog from '@/views/roles/MenuSelectTableDialog.vue'
import DataDomainSelectTableDialog from '@/views/roles/DataDomainSelectTableDialog.vue'
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
const apiSelectTableVisible = ref(false)
const apiSelectRow = ref({})
const menuSelectTableVisible = ref(false)
const menuSelectRow = ref({})
const dataDomainSelectTableVisible = ref(false)
const dataDomainSelectRow = ref({})

// init data
const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  getRolePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get role page:', error)
    ElMessage.error(error.message || 'Failed to get role page')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  getRolePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to refresh role page:', error)
    ElMessage.error(error.message || 'Failed to refresh role page')
  })
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  currentPage.value = 1 // Reset to first page on new search
  getRolePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to search role page:', error)
    ElMessage.error(error.message || 'Failed to search role page')
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
const handleDelete = (id) => {
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning',
  }).then(() => {
    deleteRoleById(id).then(() => {
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
  getRolePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
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
  getRolePage(currentPage.value, pageSize.value, searchParams.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to change current page:', error)
    ElMessage.error(error.message || 'Failed to change current page')
  })
}

// select api
const handleApiSelect = (row) => {
  apiSelectRow.value = row
  apiSelectTableVisible.value = true
}

const handleMenuSelect = (row) => {
  menuSelectRow.value = row
  menuSelectTableVisible.value = true
}

const handleDataDomainSelect = (row) => {
  dataDomainSelectRow.value = row
  dataDomainSelectTableVisible.value = true
}

// select api confirm
const handleApiSelectConfirm = () => {
  apiSelectTableVisible.value = false
  handleRefresh()
}

// select api cancel
const handleApiSelectCancel = () => {
  apiSelectTableVisible.value = false
}

// select menu confirm
const handleMenuSelectConfirm = () => {
  menuSelectTableVisible.value = false
  handleRefresh()
}

// select menu cancel
const handleMenuSelectCancel = () => {
  menuSelectTableVisible.value = false
}

const handleDataDomainSelectConfirm = () => {
  dataDomainSelectTableVisible.value = false
  handleRefresh()
}

const handleDataDomainSelectCancel = () => {
  dataDomainSelectTableVisible.value = false
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

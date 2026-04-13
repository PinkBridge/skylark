<template>
  <el-card shadow="always">
    <OrgSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" v-permission="'perm.orgs.new'"
        @click="handleCreate">{{ permLabelName('perm.orgs.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" min-width="150" />
      <el-table-column prop="code" :label="t('CodeLabel')" min-width="120" />
      <el-table-column prop="tenantName" :label="t('TenantNameLabel')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="parentOrganization" :label="t('ParentOrganizationLabel')" min-width="150">
        <template #default="{ row }">
          {{ row.parentOrganization ? (row.parentOrganization.name || row.parentOrganization) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="type" :label="t('TypeLabel')" width="140">
        <template #default="{ row }">
          <el-tag type="info">
            {{ formatType(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="t('StatusLabel')" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? t('Active') : t('Disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'perm.orgs.detail'">
            {{ permLabelName('perm.orgs.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'perm.orgs.edit'">{{ permLabelName('perm.orgs.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'perm.orgs.delete'">{{ permLabelName('perm.orgs.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <OrgDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <OrgCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <OrgEditDialog v-if="editRow && editRow.id" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOrgList, deleteOrgById } from '@/views/orgs/OrgApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import OrgSearchForm from '@/views/orgs/OrgSearchForm.vue'
import OrgDetailDialog from '@/views/orgs/OrgDetailDialog.vue'
import OrgCreateDialog from '@/views/orgs/OrgCreateDialog.vue'
import OrgEditDialog from '@/views/orgs/OrgEditDialog.vue'
import { permLabelName } from '@/utils/menuPermLabelNames'

const { t } = useI18n()

// data
const tableData = ref([])
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailRow = ref({})
const editRow = ref({})
const searchParams = ref({})

const formatType = (type) => {
  if (!type) return '-'
  if (type === 'COMPANY') return t('OrgTypeCompany')
  if (type === 'DEPARTMENT') return t('OrgTypeDepartment')
  if (type === 'TEAM') return t('OrgTypeTeam')
  return type
}

// init data
const initData = () => {
  tableData.value = []
  searchParams.value = {}
  getOrgList(searchParams.value).then(response => {
    // If response is an array, use it directly; if it's an object with data property, use that
    tableData.value = Array.isArray(response) ? response : (response.data || response.list || [])
  }).catch(error => {
    console.error('Failed to get org list:', error)
    ElMessage.error(error.message || 'Failed to get org list')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  getOrgList(searchParams.value).then(response => {
    tableData.value = Array.isArray(response) ? response : (response.data || response.list || [])
  }).catch(error => {
    console.error('Failed to refresh org list:', error)
    ElMessage.error(error.message || 'Failed to refresh org list')
  })
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  getOrgList(searchParams.value).then(response => {
    tableData.value = Array.isArray(response) ? response : (response.data || response.list || [])
  }).catch(error => {
    console.error('Failed to search org list:', error)
    ElMessage.error(error.message || 'Failed to search org list')
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
    deleteOrgById(id).then(() => {
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

// mounted
onMounted(() => {
  initData()
})
</script>

<style scoped>
.buttons-block {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}
</style>



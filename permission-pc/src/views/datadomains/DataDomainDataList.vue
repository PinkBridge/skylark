<template>
  <el-card shadow="always">
    <DataDomainSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button
        type="primary"
        size="default"
        :icon="Plus"
        v-permission="'system.data-domains.new'"
        @click="handleCreate"
      >
        {{ permLabelName('system.data-domains.new', 'NewButtonLabel') }}
      </el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">
        {{ t('RefreshButtonLabel') }}
      </el-button>
    </div>
    <el-table
      :data="tableData"
      style="width: 100%"
      stripe
      border
      show-overflow-tooltip
    >
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" min-width="150" />
      <el-table-column prop="code" :label="t('CodeLabel')" min-width="150" />
      <el-table-column prop="type" :label="t('TypeLabel')" width="150" />
      <el-table-column prop="enabled" :label="t('EnabledLabel')" width="120">
        <template #default="{ row }">
          <el-tag :type="isDataDomainRowEnabled(row.enabled) ? 'success' : 'danger'">
            {{ isDataDomainRowEnabled(row.enabled) ? t('Yes') : t('No') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tenantName" :label="t('TenantNameLabel')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="description" :label="t('DescriptionLabel')" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="180" fixed="right" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="default"
            @click="handleEdit(row)"
            v-permission="'system.data-domains.edit'"
          >
            {{ permLabelName('system.data-domains.edit', 'EditLabel') }}
          </el-button>
          <el-button
            link
            type="primary"
            size="default"
            @click="handleDelete(row.id)"
            v-permission="'system.data-domains.delete'"
          >
            {{ permLabelName('system.data-domains.delete', 'DeleteLabel') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 40]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <DataDomainCreateDialog
      :visible="createDialogVisible"
      :onSubmit="handleCreateSubmit"
      :onCancel="handleCreateCancel"
    />
    <DataDomainEditDialog
      v-if="editRow && editRow.id"
      :visible="editDialogVisible"
      :row="editRow"
      :onSubmit="handleEditSubmit"
      :onCancel="handleEditCancel"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getDataDomainPage, deleteDataDomainById } from '@/views/datadomains/DataDomainApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DataDomainSearchForm from '@/views/datadomains/DataDomainSearchForm.vue'
import DataDomainCreateDialog from '@/views/datadomains/DataDomainCreateDialog.vue'
import DataDomainEditDialog from '@/views/datadomains/DataDomainEditDialog.vue'
import { permLabelName } from '@/utils/menuPermLabelNames'

const { t } = useI18n()

/** 与后端 Boolean、DB 0/1 及 DataDomainResolutionService（null 视为启用）一致 */
function isDataDomainRowEnabled(val) {
  if (val === false || val === 0 || val === '0') {
    return false
  }
  if (val === true || val === 1 || val === '1') {
    return true
  }
  return true
}

// data
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editRow = ref({})
const searchParams = ref({})

// init data
const loadPage = () => {
  getDataDomainPage(currentPage.value, pageSize.value, searchParams.value)
    .then((response) => {
      // backend can return { records, page, size, total } or list directly
      if (Array.isArray(response)) {
        tableData.value = response
        total.value = response.length
      } else {
        tableData.value = response.records || response.list || []
        currentPage.value = response.page || currentPage.value
        pageSize.value = response.size || pageSize.value
        total.value = response.total ?? total.value
      }
    })
    .catch((error) => {
      console.error('Failed to get data domain page:', error)
      ElMessage.error(error.message || 'Failed to get data domain page')
    })
}

const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  searchParams.value = {}
  loadPage()
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  initData()
}

// refresh data
const handleRefresh = () => {
  loadPage()
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  currentPage.value = 1
  loadPage()
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

// delete record
const handleDelete = (id) => {
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning'
  })
    .then(() => {
      deleteDataDomainById(id)
        .then(() => {
          initData()
          ElMessage.success('Delete successful!')
        })
        .catch((error) => {
          console.error('Delete failed:', error)
          ElMessage.error(error.message || 'Delete failed')
        })
    })
    .catch(() => {
      // User cancelled, do nothing
    })
}

// change page size
const handleSizeChange = (size) => {
  pageSize.value = size
  loadPage()
}

// change current page
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadPage()
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



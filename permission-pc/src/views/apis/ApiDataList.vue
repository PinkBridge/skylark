<template>
  <el-card shadow="always">
    <ApiSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" 
      v-permission="'system.apis.new'"
      @click="handleCreate">{{ permLabelName('system.apis.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80"/>
      <el-table-column prop="method" :label="t('MethodLabel')" width="120">
        <template #default="{ row }">
          <el-tag
            :type="row.method === 'GET' ? 'success'
                   : row.method === 'POST' ? 'primary'
                   : row.method === 'PUT' ? 'warning'
                   : row.method === 'DELETE' ? 'danger'
                   : 'info'"
            effect="plain"
          >
            {{ row.method }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" :label="t('PathLabel')"  width="300"/>
      <el-table-column prop="permlabel" :label="t('PermLabelLabel')"  />
      <el-table-column prop="moduleKey" :label="t('ModuleKeyLabel')"  />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')"  />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')"  />
      <el-table-column :label="t('OperationsLabel')">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'system.apis.detail'">
            {{ permLabelName('system.apis.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'system.apis.edit'">{{ permLabelName('system.apis.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'system.apis.delete'">{{ permLabelName('system.apis.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <ApiDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <ApiCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <ApiEditDialog v-if="editRow && editRow.id" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
  </el-card>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getApiPage, deleteApiById } from '@/views/apis/ApiApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import ApiSearchForm from '@/views/apis/ApiSearchForm.vue'
import ApiDetailDialog from '@/views/apis/ApiDetailDialog.vue'
import ApiCreateDialog from '@/views/apis/ApiCreateDialog.vue'
import ApiEditDialog from '@/views/apis/ApiEditDialog.vue'
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

// init data
const initData = () => {
  tableData.value = []
  currentPage.value = 1
  pageSize.value = 10
  total.value = 0
  getApiPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
  })
}

// reset data
const handleReset = () => {
  initData()
}

// refresh data
const handleRefresh = () => {
  getApiPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
  })
}

// filter data
const handleSearch = (params) => {
  getApiPage(currentPage.value, pageSize.value, params).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
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
    deleteApiById(id).then(() => {
      initData()
    }).catch(error => {
      console.error('Delete failed:', error)
    })
  }).catch(() => {
    // User cancelled, do nothing
  })
}

// change page size
const handleSizeChange = (size) => {
  pageSize.value = size
  getApiPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
  })
}

// change current page
const handleCurrentChange = (page) => {
  currentPage.value = page
  getApiPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
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


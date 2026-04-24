<template>
  <el-card shadow="always">
    <WhitelistSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" 
      v-permission="'system.whitelist.new'"
      @click="handleCreate">{{ permLabelName('system.whitelist.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80"/>
      <el-table-column prop="path" :label="t('PathLabel')" min-width="100"/>
      <el-table-column prop="method" :label="t('MethodLabel')" />
      <el-table-column prop="enabled" :label="t('EnableLabel')">
        <template #default="{ row }">
          <el-tag :type="row.enabled == true ? 'success' : 'danger'">
            {{ row.enabled == true ? t('Enabled') : t('Disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" :label="t('RemarkLabel')" />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" />
      <el-table-column :label="t('OperationsLabel')" min-width="120" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'system.whitelist.detail'">
            {{ permLabelName('system.whitelist.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'system.whitelist.edit'">{{ permLabelName('system.whitelist.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'system.whitelist.delete'">{{ permLabelName('system.whitelist.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <WhitelistDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <WhitelistCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <WhitelistEditDialog v-if="editRow && editRow.id" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
  </el-card>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getWhitelistPage, deleteWhitelistById } from '@/views/whitelist/WhitelistApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import WhitelistSearchForm from '@/views/whitelist/WhitelistSearchForm.vue'
import WhitelistDetailDialog from '@/views/whitelist/WhitelistDetailDialog.vue'
import WhitelistCreateDialog from '@/views/whitelist/WhitelistCreateDialog.vue'
import WhitelistEditDialog from '@/views/whitelist/WhitelistEditDialog.vue'
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
  getWhitelistPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get whitelist list:', error)
  })
}

// reset data
const handleReset = () => {
  initData()
}

// refresh data
const handleRefresh = () => {
  getWhitelistPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get whitelist list:', error)
  })
}

// filter data
const handleSearch = (params) => {
  getWhitelistPage(currentPage.value, pageSize.value, params).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get whitelist list:', error)
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
    deleteWhitelistById(id).then(() => {
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
  getWhitelistPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get whitelist list:', error)
  })
}

// change current page
const handleCurrentChange = (page) => {
  currentPage.value = page
  getWhitelistPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records || []
    currentPage.value = response.page || currentPage.value
    pageSize.value = response.size || pageSize.value
    total.value = response.total || 0
  }).catch(error => {
    console.error('Failed to get whitelist list:', error)
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


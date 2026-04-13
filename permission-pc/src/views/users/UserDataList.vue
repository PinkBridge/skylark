<template>
  <el-card shadow="always">
    <UserSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" 
      v-permission="'perm.users.new'"
      @click="handleCreate">{{ permLabelName('perm.users.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip>
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80"/>
            <el-table-column prop="avatar" :label="t('AvatarLabel')" width="80">
        <template #default="{ row }">
          <el-image v-if="row.avatar" style="width: 40px; height: 40px" :src="row.avatar" fit="contain" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="username" :label="t('UsernameLabel')" />
      <el-table-column prop="tenantName" :label="t('TenantNameLabel')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="status" :label="t('StatusLabel')" />
      <el-table-column prop="organization" :label="t('OrganizationLabel')" ><template #default="{ row }">
          {{ row.organization ? (row.organization.name || row.organization) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="email" :label="t('EmailLabel')" />
      <el-table-column prop="phone" :label="t('PhoneLabel')" />
      
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" />
      <el-table-column :label="t('OperationsLabel')" min-width="120">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'perm.users.detail'">
            {{ permLabelName('perm.users.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'perm.users.edit'">{{ permLabelName('perm.users.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'perm.users.delete'">{{ permLabelName('perm.users.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-block">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 30, 40]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
    <UserDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <UserCreateDialog :visible="createDialogVisible" :onSubmit="handleCreateSubmit" :onCancel="handleCreateCancel" />
    <UserEditDialog v-if="editRow && editRow.id" :visible="editDialogVisible" :row="editRow"
      :onSubmit="handleEditSubmit" :onCancel="handleEditCancel" />
  </el-card>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getUserPage, deleteUserById } from '@/views/users/UserApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import UserSearchForm from '@/views/users/UserSearchForm.vue'
import UserDetailDialog from '@/views/users/UserDetailDialog.vue'
import UserCreateDialog from '@/views/users/UserCreateDialog.vue'
import UserEditDialog from '@/views/users/UserEditDialog.vue'
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
  getUserPage(currentPage.value, pageSize.value).then(response => {
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
  getUserPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
  })
}

// filter data
const handleSearch = (params) => {
  getUserPage(currentPage.value, pageSize.value, params).then(response => {
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
    deleteUserById(id).then(() => {
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
  getUserPage(currentPage.value, pageSize.value).then(response => {
    tableData.value = response.records
    currentPage.value = response.page
    pageSize.value = response.size
    total.value = response.total
  })
}

// change current page
const handleCurrentChange = (page) => {
  currentPage.value = page
  getUserPage(currentPage.value, pageSize.value).then(response => {
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

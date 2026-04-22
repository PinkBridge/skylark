<template>
  <el-card shadow="always">
    <MenuSearchForm
      :client-ids="oauthClientIds"
      :app="selectedApp"
      @update:app="onAppFilterChange"
      :search="handleSearch"
      :reset="handleReset"
    />
    <div class="buttons-block">
      <el-button type="primary" size="default" :icon="Plus" 
      v-permission="'system.menus.new'"
      @click="handleCreate">{{ permLabelName('system.menus.new', 'NewButtonLabel') }}</el-button>
      <el-button type="default" size="default" :icon="Refresh" @click="handleRefresh">{{
        t('RefreshButtonLabel') }}</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" ref="menuTableRef">
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" width="150" />
      <el-table-column prop="sort" :label="t('SortLabel')" width="100" />
      <el-table-column prop="icon" :label="t('IconLabel')" width="80" >
        <template #default="{ row }">
          <el-icon v-if="row.icon && Icons[row.icon]" :size="20">
            <component :is="Icons[row.icon]" />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="type" :label="t('TypeLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type == 'menu' ? 'success' : 'danger'">
            {{ row.type == 'menu' ? t('MenuLabel') : t('ButtonLabel') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" :label="t('PathLabel')" min-width="150" />
      <el-table-column prop="permlabel" :label="t('PermLabel')" min-width="150" />
      <el-table-column prop="moduleKey" :label="t('ModuleKeyLabel')" min-width="120" />
      <el-table-column prop="appCode" :label="t('AppCodeLabel')" min-width="130" />
      <el-table-column prop="hidden" :label="t('HiddenLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.hidden == true ? 'danger' : 'success'">
            {{ row.hidden == true ? t('Yes') : t('No') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="180" />
      <el-table-column :label="t('OperationsLabel')" min-width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="default" @click="handleDetail(row)" v-permission="'system.menus.detail'">
            {{ permLabelName('system.menus.detail', 'DetailLabel') }}
          </el-button>
          <el-button link type="primary" size="default" @click="handleEdit(row)" v-permission="'system.menus.edit'">{{ permLabelName('system.menus.edit', 'EditLabel') }}</el-button>
          <el-button link type="primary" size="default" @click="handleDelete(row.id)" v-permission="'system.menus.delete'">{{ permLabelName('system.menus.delete', 'DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <MenuDetailDialog v-if="detailRow && detailRow.id" :visible="detailDialogVisible" :row="detailRow"
      :onConfirm="handleDetailConfirm" />
    <MenuCreateDialog
      :visible="createDialogVisible"
      :client-ids="oauthClientIds"
      :default-app-code="selectedApp"
      :onSubmit="handleCreateSubmit"
      :onCancel="handleCreateCancel"
    />
    <MenuEditDialog
      v-if="editRow && editRow.id"
      :visible="editDialogVisible"
      :row="editRow"
      :client-ids="oauthClientIds"
      :onSubmit="handleEditSubmit"
      :onCancel="handleEditCancel"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { getMenuList, deleteMenuById } from '@/views/menus/MenuApi'
import { getAppList } from '@/views/apps/AppApi'
import { Refresh, Plus } from '@element-plus/icons-vue'
import * as Icons from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import MenuSearchForm from '@/views/menus/MenuSearchForm.vue'
import MenuDetailDialog from '@/views/menus/MenuDetailDialog.vue'
import MenuCreateDialog from '@/views/menus/MenuCreateDialog.vue'
import MenuEditDialog from '@/views/menus/MenuEditDialog.vue'
import { permLabelName } from '@/utils/menuPermLabelNames'

const { t } = useI18n()

// data
const tableData = ref([])
const menuTableRef = ref(null)
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailRow = ref({})
const editRow = ref({})
const searchParams = ref({})
const oauthClientIds = ref([])
const selectedApp = ref('')

const onAppFilterChange = (v) => {
  selectedApp.value = v || ''
}

// Expand first level only
const expandFirstLevel = () => {
  const table = menuTableRef.value
  if (!table || !Array.isArray(tableData.value)) return
  
  nextTick(() => {
    tableData.value.forEach(rootNode => {
      table.toggleRowExpansion(rootNode, true)
    })
  })
}

const normalizeMenuTreeResponse = (response) =>
  Array.isArray(response) ? response : (response.data || response.list || [])

// init data
const initData = () => {
  const app = selectedApp.value || oauthClientIds.value[0] || ''
  searchParams.value = { ...searchParams.value, app }
  getMenuList(searchParams.value).then((response) => {
    tableData.value = normalizeMenuTreeResponse(response)
    expandFirstLevel()
  }).catch((error) => {
    console.error('Failed to get menu list:', error)
    ElMessage.error(error.message || 'Failed to get menu list')
  })
}

// reset data
const handleReset = (params) => {
  searchParams.value = params
  selectedApp.value = params.app || ''
  initData()
}

// refresh data
const handleRefresh = () => {
  const app = selectedApp.value || oauthClientIds.value[0] || ''
  searchParams.value = { ...searchParams.value, app }
  getMenuList(searchParams.value).then((response) => {
    tableData.value = normalizeMenuTreeResponse(response)
    expandFirstLevel()
  }).catch(error => {
    console.error('Failed to refresh menu list:', error)
    ElMessage.error(error.message || 'Failed to refresh menu list')
  })
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  if (params.app !== undefined && params.app !== null) {
    selectedApp.value = params.app
  }
  getMenuList(searchParams.value).then((response) => {
    tableData.value = normalizeMenuTreeResponse(response)
    expandFirstLevel()
  }).catch(error => {
    console.error('Failed to search menu list:', error)
    ElMessage.error(error.message || 'Failed to search menu list')
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
    deleteMenuById(id).then(() => {
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
onMounted(async () => {
  try {
    const list = await getAppList()
    const rows = Array.isArray(list) ? list : []
    oauthClientIds.value = rows
      .map((c) => c.clientId)
      .filter(Boolean)
      .sort()
    selectedApp.value = oauthClientIds.value[0] || ''
    searchParams.value = { app: selectedApp.value }
  } catch (e) {
    console.error('Failed to load OAuth clients:', e)
    ElMessage.error(e.message || 'Failed to load OAuth clients')
  }
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


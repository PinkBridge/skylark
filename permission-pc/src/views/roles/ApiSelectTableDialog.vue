<template>
  <el-dialog :model-value="visible" :title="t('SelectTitle')" align-center destroy-on-close :show-close="false"
    :modal="false" modal-penetrable width="80%">
    <ApiSearchForm :search="handleSearch" :reset="handleReset" />
    <el-table ref="apiTableRef" row-key="id" :data="tableData" style="width: 100%" stripe border
      show-overflow-tooltip selection-mode="multiple"
      @selection-change="handleSelectionChange" @select-all="handleSelectAll">
      <el-table-column type="selection" width="55" />
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
      <el-table-column prop="path" :label="t('PathLabel')" max-width="120" />
      <el-table-column prop="permlabel" :label="t('PermLabelLabel')" max-width="120" />
      <el-table-column prop="moduleKey" :label="t('ModuleKeyLabel')" max-width="120" />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" max-width="120" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" max-width="120" />
    </el-table>
    <!-- grantable list is ceiling-limited; pagination removed for v1 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="confirmLoading" @click="handleConfirm">{{ t('ConfirmButtonText') }}</el-button>
        <el-button type="default" @click="onCancel">{{ t('CancelButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>

</template>

<script setup name="ApiSelectTableDialog">
import { ref, watch, onMounted, nextTick } from 'vue'    
import { useI18n } from 'vue-i18n'
import { getGrantableApis } from '@/views/apis/ApiApi'
import ApiSearchForm from '@/views/apis/ApiSearchForm.vue'
import { getRoleById, bindRoleApis } from '@/views/roles/RoleApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm', 'onCancel'])

// data
const tableData = ref([])
const multipleSelection = ref([])
const apiTableRef = ref(null)
const confirmLoading = ref(false)

// init data
const initData = () => {
  tableData.value = []

  getGrantableApis().then(response => {
    tableData.value = Array.isArray(response) ? response : (response.records || response.data || [])
    getRoleById(props.row.id).then(response => {
      const apiIds = response.apiIds
      nextTick(() => {
        setDefaultSelection(apiIds)
      })
    })
  })
}

// reset data
const handleReset = () => {
  initData()
}

// filter data
const handleSearch = (params) => {
  getGrantableApis().then(response => {
    const rows = Array.isArray(response) ? response : (response.records || response.data || [])
    const q = params || {}
    tableData.value = rows.filter(r => {
      const methodOk = !q.method || String(r.method || '').includes(q.method)
      const pathOk = !q.path || String(r.path || '').includes(q.path)
      const permOk = !q.permlabel || String(r.permlabel || '').includes(q.permlabel)
      const moduleOk = !q.moduleKey || String(r.moduleKey || '').includes(q.moduleKey)
      return methodOk && pathOk && permOk && moduleOk
    })
    getRoleById(props.row.id).then(response => {
      const apiIds = response.apiIds
      nextTick(() => {
        setDefaultSelection(apiIds)
      })
    })
  })
}

// cancel
const onCancel = () => {
  props.onCancel()
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

/** 表头「全选」：保证当前表格中的记录全部被勾选 */
const handleSelectAll = (selection) => {
  const table = apiTableRef.value
  if (!table || !tableData.value.length) {
    return
  }
  if (!selection || selection.length === 0) {
    return
  }
  nextTick(() => {
    tableData.value.forEach((row) => {
      table.toggleRowSelection(row, true)
    })
  })
}

const handleConfirm = () => {
  const table = apiTableRef.value
  if (!table || !props.row?.id) {
    return
  }
  const rows = table.getSelectionRows()
  const apiIds = rows.map((r) => r.id)
  confirmLoading.value = true
  bindRoleApis(props.row.id, apiIds)
    .then(() => {
      ElMessage.success(t('UpdateSuccess'))
      props.onConfirm()
    })
    .catch((error) => {
      ElMessage.error(error?.message || error || 'Save failed')
    })
    .finally(() => {
      confirmLoading.value = false
    })
}

const setDefaultSelection = (apiIds) => {
  const table = apiTableRef.value
  if (!table || !Array.isArray(tableData.value) || !Array.isArray(apiIds)) {
    return
  }

  tableData.value.forEach(row => {
    table.toggleRowSelection(row, false)
  })

  nextTick(() => {
    apiIds.forEach(id => {
      const row = tableData.value.find(row => row.id === id)
      if (row) {
        table.toggleRowSelection(row, true)
      }
    })
  })
}

onMounted(() => {
  initData()
})

// Watch for dialog visibility changes
watch(() => props.visible, (newVal) => {
  if (newVal && props.row?.id) {
    // Dialog opened, reload data
    initData()
  }
})
</script>

<style scoped>

.buttons-block {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}
</style>

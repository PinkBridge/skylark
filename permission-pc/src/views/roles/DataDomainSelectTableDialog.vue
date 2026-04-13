<template>
  <el-dialog :model-value="visible" :title="t('SelectTitle')" align-center destroy-on-close :show-close="false"
    :modal="false" modal-penetrable width="80%">
    <DataDomainSearchForm :search="handleSearch" :reset="handleReset" />
    <el-table ref="tableRef" row-key="id" :data="tableData" style="width: 100%" stripe border
      show-overflow-tooltip selection-mode="multiple"
      @selection-change="handleSelectionChange" @select-all="handleSelectAll">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" min-width="140" />
      <el-table-column prop="code" :label="t('CodeLabel')" min-width="140" />
      <el-table-column prop="type" :label="t('TypeLabel')" width="140" />
      <el-table-column prop="enabled" :label="t('EnabledLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled === true || row.enabled === 1 ? 'success' : 'danger'">
            {{ row.enabled === true || row.enabled === 1 ? t('Yes') : t('No') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" :label="t('RemarkLabel')" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="170" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="170" />
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="confirmLoading" @click="handleConfirm">{{ t('ConfirmButtonText') }}</el-button>
        <el-button type="default" @click="onCancel">{{ t('CancelButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="DataDomainSelectTableDialog">
import { ref, watch, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { getGrantableDataDomains } from '@/views/datadomains/DataDomainApi'
import DataDomainSearchForm from '@/views/datadomains/DataDomainSearchForm.vue'
import { getRoleById, bindRoleDataDomains } from '@/views/roles/RoleApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm', 'onCancel'])

const tableData = ref([])
const multipleSelection = ref([])
const tableRef = ref(null)
const confirmLoading = ref(false)
const allRows = ref([])

const initData = () => {
  tableData.value = []
  allRows.value = []
  getGrantableDataDomains(props.row?.id).then((response) => {
    const rows = Array.isArray(response) ? response : (response.records || response.data || [])
    allRows.value = rows
    tableData.value = rows
    getRoleById(props.row.id).then((roleDto) => {
      const ids = roleDto?.dataDomainIds || []
      nextTick(() => {
        setDefaultSelection(ids)
      })
    })
  })
}

const handleReset = () => {
  initData()
}

const rowEnabledMatch = (row, qEnabled) => {
  if (qEnabled === '' || qEnabled === null || qEnabled === undefined) {
    return true
  }
  const v = row.enabled === true || row.enabled === 1 ? 1 : 0
  return v === Number(qEnabled)
}

const handleSearch = (params) => {
  getGrantableDataDomains(props.row?.id).then((response) => {
    const rows = Array.isArray(response) ? response : (response.records || response.data || [])
    allRows.value = rows
    const q = params || {}
    tableData.value = rows.filter((r) => {
      const nameOk = !q.name || String(r.name || '').includes(String(q.name))
      const codeOk = !q.code || String(r.code || '').includes(String(q.code))
      const typeOk = !q.type || String(r.type || '') === String(q.type)
      const enabledOk = rowEnabledMatch(r, q.enabled)
      return nameOk && codeOk && typeOk && enabledOk
    })
    getRoleById(props.row.id).then((roleDto) => {
      const ids = roleDto?.dataDomainIds || []
      nextTick(() => {
        setDefaultSelection(ids)
      })
    })
  })
}

const onCancel = () => {
  props.onCancel()
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const handleSelectAll = (selection) => {
  const table = tableRef.value
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
  const table = tableRef.value
  if (!table || !props.row?.id) {
    return
  }
  const rows = table.getSelectionRows()
  const ids = rows.map((r) => r.id)
  confirmLoading.value = true
  bindRoleDataDomains(props.row.id, ids)
    .then(() => {
      ElMessage.success(t('UpdateSuccess'))
      props.onConfirm()
    })
    .catch((error) => {
      const msg = typeof error === 'string' ? error : (error?.message || 'Save failed')
      ElMessage.error(msg)
    })
    .finally(() => {
      confirmLoading.value = false
    })
}

const setDefaultSelection = (domainIds) => {
  const table = tableRef.value
  if (!table || !Array.isArray(tableData.value) || !Array.isArray(domainIds)) {
    return
  }
  tableData.value.forEach((row) => {
    table.toggleRowSelection(row, false)
  })
  nextTick(() => {
    domainIds.forEach((id) => {
      const row = tableData.value.find((r) => r.id === id)
      if (row) {
        table.toggleRowSelection(row, true)
      }
    })
  })
}

onMounted(() => {
  initData()
})

watch(() => props.visible, (newVal) => {
  if (newVal && props.row?.id) {
    initData()
  }
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

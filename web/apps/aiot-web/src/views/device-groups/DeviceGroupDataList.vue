<template>
  <el-card shadow="always">
    <DeviceGroupSearchForm :search="handleSearch" :reset="handleReset" />
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="openCreate">{{ t('NewButtonLabel') }}</el-button>
        <el-button :icon="Refresh" @click="loadGroups">{{ t('RefreshButtonLabel') }}</el-button>
      </div>
    </div>

    <el-table :data="tableData" stripe border empty-text=" ">
      <template #empty>
        <el-empty :description="t('DeviceRecordsEmpty')" />
      </template>
      <el-table-column prop="name" :label="t('DeviceGroupNameLabel')" min-width="220" />
      <el-table-column prop="groupKey" :label="t('DeviceGroupKeyLabel')" min-width="200" show-overflow-tooltip />
      <el-table-column :label="t('DeviceGroupDeviceCountLabel')" width="120" align="right">
        <template #default="{ row }">
          {{ deviceCountOf(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="description" :label="t('DescriptionLabel')" min-width="280" show-overflow-tooltip />
      <el-table-column :label="t('OperationsLabel')" min-width="360" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openManageDevices(row)">{{ t('DeviceGroupManageDevicesLabel') }}</el-button>
          <el-button link type="primary" @click="openEdit(row)">{{ t('EditLabel') }}</el-button>
          <el-button link type="primary" @click="confirmDelete(row)">{{ t('DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="loadGroups"
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog :model-value="editVisible" :title="editingGroupKey ? t('EditTitle') : t('CreateTitle')" align-center destroy-on-close @close="closeEdit">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="t('DeviceGroupNameLabel')" prop="name">
          <el-input v-model="form.name" :placeholder="t('DeviceGroupNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('DescriptionLabel')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="t('DescriptionPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeEdit">{{ t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="submitEdit">{{ t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <DeviceGroupDeviceSelectDialog
      :visible="deviceSelectVisible"
      :group="deviceSelectGroup"
      :on-cancel="closeDeviceSelect"
      :on-submitted="handleDeviceSelectSubmitted"
    />
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createDeviceGroup,
  deleteDeviceGroup,
  listDeviceGroups,
  updateDeviceGroup
} from '@/views/device-groups/DeviceGroupApi'
import DeviceGroupSearchForm from '@/views/device-groups/DeviceGroupSearchForm.vue'
import DeviceGroupDeviceSelectDialog from '@/views/device-groups/DeviceGroupDeviceSelectDialog.vue'

const { t } = useI18n()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchParams = ref({
  groupKey: '',
  name: ''
})
const editVisible = ref(false)
const editingGroupKey = ref('')
const formRef = ref(null)
const form = ref({ name: '', description: '' })

const deviceSelectVisible = ref(false)
const deviceSelectGroup = ref(null)

const rules = computed(() => ({
  name: [{ required: true, message: t('DeviceGroupNameRequired'), trigger: 'blur' }]
}))

/** 兼容 camelCase / snake_case；缺省按 0 展示，避免列空白 */
function deviceCountOf(row) {
  const v = row?.deviceCount ?? row?.device_count
  if (v === null || v === undefined || v === '') return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

/** 兼容分页对象 { records, total, pageNum, pageSize } 与旧版直接返回数组 */
function normalizeGroupListResponse(raw, fallbackPageNum, fallbackPageSize) {
  if (Array.isArray(raw)) {
    const list = raw
    return {
      records: list,
      total: list.length,
      pageNum: 1,
      pageSize: Math.max(list.length, 1)
    }
  }
  const records = Array.isArray(raw?.records) ? raw.records : []
  let total = typeof raw?.total === 'number' ? raw.total : Number(raw?.total)
  if (!Number.isFinite(total)) total = records.length
  let pn = Number(raw?.pageNum)
  if (!Number.isFinite(pn) || pn < 1) pn = fallbackPageNum
  let ps = Number(raw?.pageSize)
  if (!Number.isFinite(ps) || ps < 1) ps = fallbackPageSize
  return { records, total, pageNum: pn, pageSize: ps }
}

const loadGroups = async () => {
  try {
    const safePageNum = Math.max(1, Number(pageNum.value) || 1)
    const gk = (searchParams.value.groupKey || '').trim()
    const nm = (searchParams.value.name || '').trim()
    const params = {
      pageNum: safePageNum,
      pageSize: pageSize.value
    }
    if (gk) params.groupKey = gk
    if (nm) params.name = nm

    const data = await listDeviceGroups(params)
    const norm = normalizeGroupListResponse(data, safePageNum, pageSize.value)
    tableData.value = norm.records
    total.value = norm.total
    pageNum.value = norm.pageNum
    pageSize.value = norm.pageSize
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

const handleSearch = (params) => {
  searchParams.value = { groupKey: params.groupKey || '', name: params.name || '' }
  pageNum.value = 1
  loadGroups()
}

const handleReset = (params) => {
  searchParams.value = { groupKey: params.groupKey || '', name: params.name || '' }
  pageNum.value = 1
  loadGroups()
}

const handlePageSizeChange = (value) => {
  pageSize.value = value
  pageNum.value = 1
  loadGroups()
}

const openCreate = () => {
  editingGroupKey.value = ''
  form.value = { name: '', description: '' }
  editVisible.value = true
}

const openEdit = (row) => {
  editingGroupKey.value = row.groupKey
  form.value = { name: row.name || '', description: row.description || '' }
  editVisible.value = true
}

const closeEdit = () => {
  editVisible.value = false
  editingGroupKey.value = ''
  form.value = { name: '', description: '' }
}

const submitEdit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (editingGroupKey.value) {
      await updateDeviceGroup(editingGroupKey.value, form.value)
      ElMessage.success(t('UpdateSuccess'))
    } else {
      await createDeviceGroup(form.value)
      ElMessage.success(t('CreateSuccess'))
      pageNum.value = 1
    }
    closeEdit()
    await loadGroups()
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  }
}

const confirmDelete = (row) => {
  ElMessageBox.confirm(t('DeleteConfirmLabel'), t('RemoveTitle'), {
    confirmButtonText: t('ConfirmButtonText'),
    cancelButtonText: t('CancelButtonText'),
    type: 'warning'
  }).then(async () => {
    await deleteDeviceGroup(row.groupKey)
    ElMessage.success(t('DeleteSuccess'))
    if (tableData.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    await loadGroups()
  }).catch(() => {})
}

const openManageDevices = (row) => {
  deviceSelectGroup.value = row
  deviceSelectVisible.value = true
}

const closeDeviceSelect = () => {
  deviceSelectVisible.value = false
  deviceSelectGroup.value = null
}

const handleDeviceSelectSubmitted = async () => {
  closeDeviceSelect()
  await loadGroups()
}

onMounted(() => loadGroups())
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar-left {
  display: inline-flex;
  gap: 12px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>


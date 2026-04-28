<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="920px"
    align-center
    destroy-on-close
    @close="props.onCancel()"
  >
    <div class="dialog-body">
      <el-input
        v-model="keyword"
        :placeholder="t('DeviceGroupDeviceSearchPlaceholder')"
        clearable
        class="search-input"
        @input="onKeywordInput"
        @clear="onKeywordClear"
      />
      <el-table
        ref="tableRef"
        :data="devices"
        :row-key="deviceRowKey"
        stripe
        border
        height="400"
        empty-text=" "
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <el-empty :description="t('DeviceRecordsEmpty')" />
        </template>
        <el-table-column type="selection" width="48" reserve-selection />
        <el-table-column prop="deviceName" :label="t('DeviceNameLabel')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="deviceKey" :label="t('DeviceKeyLabel')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="productKey" :label="t('ProductKeyLabel')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="deviceType" :label="t('DeviceTypeLabel')" min-width="140" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="loadDevicePage"
          @size-change="onPageSizeChange"
        />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onCancel()">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" :disabled="!isSelectionDirty" @click="submit">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { listDevicesPage } from '@/views/devices/DeviceApi'
import { addDevicesToGroup, listGroupDevices, removeDeviceFromGroup } from '@/views/device-groups/DeviceGroupApi'

const props = defineProps(['visible', 'group', 'onCancel', 'onSubmitted'])
const { t } = useI18n()

const tableRef = ref(null)
const devices = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const keyword = ref('')
/** productKey::deviceKey -> { productKey, deviceKey }，跨页保留勾选 */
const selectedByKey = ref({})
const initialMembers = ref([])
let keywordDebounceTimer = null
let selectionSyncing = false

const deviceRowKey = (row) => `${row.productKey}::${row.deviceKey}`

const memberKey = (d) => `${d.productKey}::${d.deviceKey}`

const isSelectionDirty = computed(() => {
  const ini = new Set(initialMembers.value.map(memberKey))
  const sel = new Set(Object.keys(selectedByKey.value))
  if (ini.size !== sel.size) return true
  for (const k of sel) {
    if (!ini.has(k)) return true
  }
  return false
})

const dialogTitle = computed(() => {
  const name = props.group?.name || '-'
  return `${t('DeviceGroupManageDevicesLabel')} - ${name}`
})

const syncTableSelectionFromKeys = async () => {
  await nextTick()
  await nextTick()
  const table = tableRef.value
  if (!table) return
  selectionSyncing = true
  try {
    for (const row of devices.value) {
      const k = memberKey(row)
      table.toggleRowSelection(row, !!selectedByKey.value[k])
    }
  } finally {
    selectionSyncing = false
  }
}

const loadDevicePage = async () => {
  if (!props.visible) return
  try {
    const safePage = Math.max(1, Number(pageNum.value) || 1)
    pageNum.value = safePage
    const kw = (keyword.value || '').trim()
    const data = await listDevicesPage({
      keyword: kw || undefined,
      pageNum: safePage,
      pageSize: pageSize.value
    })
    devices.value = data.records || []
    total.value = typeof data.total === 'number' ? data.total : Number(data.total) || 0
    pageNum.value = data.pageNum || safePage
    pageSize.value = data.pageSize || pageSize.value
    await syncTableSelectionFromKeys()
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

const onKeywordInput = () => {
  if (!props.visible) return
  if (keywordDebounceTimer) clearTimeout(keywordDebounceTimer)
  keywordDebounceTimer = setTimeout(() => {
    pageNum.value = 1
    loadDevicePage()
  }, 400)
}

const onKeywordClear = () => {
  if (keywordDebounceTimer) clearTimeout(keywordDebounceTimer)
  pageNum.value = 1
  loadDevicePage()
}

const onPageSizeChange = () => {
  pageNum.value = 1
  loadDevicePage()
}

const loadDialogData = async () => {
  const groupKey = props.group?.groupKey
  if (!groupKey) return
  try {
    const members = await listGroupDevices(groupKey)
    const memberList = Array.isArray(members) ? members : []
    initialMembers.value = memberList.map((m) => ({
      productKey: m.productKey,
      deviceKey: m.deviceKey
    }))
    const sel = {}
    for (const m of initialMembers.value) {
      sel[memberKey(m)] = { productKey: m.productKey, deviceKey: m.deviceKey }
    }
    selectedByKey.value = sel
    pageNum.value = 1
    await loadDevicePage()
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

const handleSelectionChange = (rows) => {
  if (selectionSyncing) return
  const pageKeys = devices.value.map(memberKey)
  const next = { ...selectedByKey.value }
  for (const k of pageKeys) {
    delete next[k]
  }
  for (const row of rows) {
    next[memberKey(row)] = { productKey: row.productKey, deviceKey: row.deviceKey }
  }
  selectedByKey.value = next
}

const submit = async () => {
  const groupKey = props.group?.groupKey
  if (!groupKey) return
  const selectedSet = new Set(Object.keys(selectedByKey.value))
  const initialSet = new Set(initialMembers.value.map(memberKey))
  const toRemove = initialMembers.value.filter((m) => !selectedSet.has(memberKey(m)))
  const toAdd = Object.values(selectedByKey.value).filter((pair) => !initialSet.has(memberKey(pair)))
  if (!toRemove.length && !toAdd.length) return
  try {
    await Promise.all(
      toRemove.map((m) => removeDeviceFromGroup(groupKey, m.productKey, m.deviceKey))
    )
    if (toAdd.length) {
      await addDevicesToGroup(
        groupKey,
        toAdd.map((d) => ({ productKey: d.productKey, deviceKey: d.deviceKey }))
      )
    }
    ElMessage.success(t('UpdateSuccess'))
    props.onSubmitted()
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  }
}

watch(
  () => props.visible,
  (v) => {
    if (v) {
      if (keywordDebounceTimer) clearTimeout(keywordDebounceTimer)
      keyword.value = ''
      pageNum.value = 1
      pageSize.value = 20
      initialMembers.value = []
      selectedByKey.value = {}
      devices.value = []
      total.value = 0
      loadDialogData()
    }
  }
)

onMounted(() => {
  if (props.visible) loadDialogData()
})
</script>

<style scoped>
.search-input {
  margin-bottom: 12px;
}

.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>


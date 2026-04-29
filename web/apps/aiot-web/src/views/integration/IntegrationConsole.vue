<template>
  <el-card shadow="always">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane :label="t('IntegrationChannelsTab')" name="channels">
        <div class="toolbar">
          <el-button v-permission="'iot.integration.edit'" type="primary" :icon="Plus" @click="openChannelCreate">
            {{ t('NewButtonLabel') }}
          </el-button>
          <el-button :icon="Refresh" @click="loadChannels">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="channelRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('IntegrationEmptyChannels')" />
          </template>
          <el-table-column prop="name" :label="t('IntegrationChannelName')" min-width="140" />
          <el-table-column prop="type" :label="t('IntegrationChannelType')" width="110" />
          <el-table-column :label="t('StatusLabel')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? t('Enabled') : t('Disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" :label="t('IntegrationUpdatedAt')" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('OperationsLabel')" width="280" fixed="right">
            <template #default="{ row }">
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="openChannelEdit(row)">
                {{ t('EditLabel') }}
              </el-button>
              <el-button
                v-permission="'iot.integration.edit'"
                link
                type="primary"
                :disabled="String(row.type).toUpperCase() !== 'WEBHOOK'"
                @click="runChannelTest(row)"
              >
                {{ t('IntegrationTestWebhook') }}
              </el-button>
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="confirmDeleteChannel(row)">
                {{ t('DeleteLabel') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="channelPage.pageNum"
            v-model:page-size="channelPage.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :total="channelPage.total"
            :page-sizes="[10, 20, 50]"
            @current-change="loadChannels"
            @size-change="loadChannels"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('IntegrationSubscriptionsTab')" name="subscriptions">
        <div class="sub-toolbar">
          <span class="sub-label">{{ t('IntegrationSelectChannel') }}</span>
          <el-select
            v-model="subChannelId"
            filterable
            clearable
            :placeholder="t('IntegrationSelectChannel')"
            style="width: 320px"
            @change="loadSubscriptions"
          >
            <el-option v-for="c in channelOptions" :key="c.id" :label="`${c.name} (#${c.id})`" :value="c.id" />
          </el-select>
          <el-button v-permission="'iot.integration.edit'" type="primary" :icon="Plus" :disabled="!subChannelId" @click="openSubCreate">
            {{ t('NewButtonLabel') }}
          </el-button>
          <el-button :icon="Refresh" :disabled="!subChannelId" @click="loadSubscriptions">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="subRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="subChannelId ? t('IntegrationEmptySubscriptions') : t('IntegrationPickChannelFirst')" />
          </template>
          <el-table-column prop="name" :label="t('NameLabel')" min-width="120" show-overflow-tooltip />
          <el-table-column :label="t('StatusLabel')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? t('Enabled') : t('Disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('IntegrationEventTypes')" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="et in row.eventTypes || []" :key="et" size="small" class="tag-gap">{{ et }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('OperationsLabel')" width="160" fixed="right">
            <template #default="{ row }">
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="openSubEdit(row)">{{ t('EditLabel') }}</el-button>
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="confirmDeleteSub(row)">{{ t('DeleteLabel') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="subPage.pageNum"
            v-model:page-size="subPage.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :total="subPage.total"
            :page-sizes="[10, 20, 50]"
            :disabled="!subChannelId"
            @current-change="loadSubscriptions"
            @size-change="loadSubscriptions"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('IntegrationDeliveriesTab')" name="deliveries">
        <div class="toolbar">
          <el-button :icon="Refresh" @click="loadDeliveries">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="deliveryRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('IntegrationEmptyDeliveries')" />
          </template>
          <el-table-column prop="eventId" :label="t('IntegrationEventId')" min-width="200" show-overflow-tooltip />
          <el-table-column prop="eventType" :label="t('IntegrationEventType')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" :label="t('IntegrationDeliveryStatus')" width="100" />
          <el-table-column prop="attempts" :label="t('IntegrationAttempts')" width="90" align="right" />
          <el-table-column prop="httpStatus" :label="t('IntegrationHttpStatus')" width="100" align="right" />
          <el-table-column prop="lastError" :label="t('IntegrationLastError')" min-width="160" show-overflow-tooltip />
          <el-table-column prop="createdAt" :label="t('IntegrationCreatedAt')" min-width="160" />
          <el-table-column :label="t('OperationsLabel')" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-permission="'iot.integration.edit'"
                link
                type="primary"
                :disabled="row.status === 'success' || row.status === 'dead'"
                @click="runRetry(row)"
              >
                {{ t('IntegrationRetry') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="deliveryPage.pageNum"
            v-model:page-size="deliveryPage.pageSize"
            background
            layout="total, sizes, prev, pager, next"
            :total="deliveryPage.total"
            :page-sizes="[10, 20, 50]"
            @current-change="loadDeliveries"
            @size-change="loadDeliveries"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="channelDialog.visible" :title="channelDialog.id ? t('EditTitle') : t('CreateTitle')" width="560px" destroy-on-close @closed="resetChannelForm">
      <el-form ref="channelFormRef" :model="channelForm" :rules="channelRules" label-position="top">
        <el-form-item :label="t('IntegrationChannelName')" prop="name">
          <el-input v-model="channelForm.name" />
        </el-form-item>
        <el-form-item :label="t('IntegrationChannelType')" prop="type">
          <el-select v-model="channelForm.type" :disabled="!!channelDialog.id" style="width: 100%">
            <el-option label="WEBHOOK" value="WEBHOOK" />
            <el-option label="MQTT" value="MQTT" />
            <el-option label="KAFKA" value="KAFKA" />
            <el-option label="RABBIT" value="RABBIT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-switch v-model="channelForm.enabled" />
        </el-form-item>
        <el-form-item :label="t('IntegrationConfigJson')" prop="configJson">
          <el-input v-model="channelForm.configJson" type="textarea" :rows="8" :placeholder="t('IntegrationConfigJsonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="channelDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="submitChannel">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="subDialog.visible" :title="subDialog.id ? t('EditTitle') : t('CreateTitle')" width="640px" destroy-on-close @closed="resetSubForm">
      <el-form ref="subFormRef" :model="subForm" :rules="subRules" label-position="top">
        <el-form-item :label="t('NameLabel')" prop="name">
          <el-input v-model="subForm.name" :placeholder="t('IntegrationSubscriptionNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-switch v-model="subForm.enabled" />
        </el-form-item>
        <el-form-item :label="t('IntegrationEventTypes')" prop="eventTypes">
          <el-select v-model="subForm.eventTypes" multiple filterable style="width: 100%">
            <el-option v-for="et in eventTypeOptions" :key="et" :label="et" :value="et" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('IntegrationFilterJson')">
          <el-input v-model="subForm.filterJson" type="textarea" :rows="3" :placeholder="t('IntegrationFilterJsonPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('IntegrationTransformJson')">
          <el-input v-model="subForm.transformJson" type="textarea" :rows="3" :placeholder="t('IntegrationTransformJsonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="submitSub">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createChannel,
  createSubscription,
  deleteChannel,
  deleteSubscription,
  listChannels,
  listDeliveries,
  listEventTypes,
  listSubscriptions,
  retryDelivery,
  testChannel,
  updateChannel,
  updateSubscription
} from '@/views/integration/AppIntegrationApi'

const { t } = useI18n()
const activeTab = ref('channels')
const eventTypeOptions = ref([])

const channelRows = ref([])
const channelPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const channelOptions = ref([])

const subChannelId = ref(null)
const subRows = ref([])
const subPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const deliveryRows = ref([])
const deliveryPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const channelDialog = reactive({ visible: false, id: null })
const channelFormRef = ref(null)
const channelForm = reactive({
  name: '',
  type: 'WEBHOOK',
  enabled: true,
  configJson: ''
})

const subDialog = reactive({ visible: false, id: null })
const subFormRef = ref(null)
const subForm = reactive({
  name: '',
  enabled: true,
  eventTypes: [],
  filterJson: '',
  transformJson: ''
})

const channelRules = computed(() => ({
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  type: [{ required: true, message: t('IntegrationTypeRequired'), trigger: 'change' }],
  configJson: [{ required: true, message: t('IntegrationConfigRequired'), trigger: 'blur' }]
}))

const subRules = computed(() => ({
  eventTypes: [{ required: true, message: t('IntegrationEventTypesRequired'), trigger: 'change' }]
}))

function parsePage(raw, fallback) {
  const records = Array.isArray(raw?.records) ? raw.records : []
  const total = Number.isFinite(Number(raw?.total)) ? Number(raw.total) : records.length
  return { records, total }
}

async function loadEventTypes() {
  try {
    const list = await listEventTypes()
    eventTypeOptions.value = Array.isArray(list) ? list : []
  } catch {
    eventTypeOptions.value = []
  }
}

async function loadChannelOptions() {
  try {
    const raw = await listChannels({ pageNum: 1, pageSize: 100 })
    const { records } = parsePage(raw, channelPage)
    channelOptions.value = records
  } catch {
    channelOptions.value = []
  }
}

async function loadChannels() {
  try {
    const raw = await listChannels({ pageNum: channelPage.pageNum, pageSize: channelPage.pageSize })
    const { records, total } = parsePage(raw, channelPage)
    channelRows.value = records
    channelPage.total = total
  } catch {
    channelRows.value = []
  }
}

async function loadSubscriptions() {
  if (!subChannelId.value) {
    subRows.value = []
    subPage.total = 0
    return
  }
  try {
    const raw = await listSubscriptions({
      channelId: subChannelId.value,
      pageNum: subPage.pageNum,
      pageSize: subPage.pageSize
    })
    const { records, total } = parsePage(raw, subPage)
    subRows.value = records
    subPage.total = total
  } catch {
    subRows.value = []
  }
}

async function loadDeliveries() {
  try {
    const raw = await listDeliveries({ pageNum: deliveryPage.pageNum, pageSize: deliveryPage.pageSize })
    const { records, total } = parsePage(raw, deliveryPage)
    deliveryRows.value = records
    deliveryPage.total = total
  } catch {
    deliveryRows.value = []
  }
}

function onTabChange(name) {
  if (name === 'subscriptions') {
    loadChannelOptions()
    loadSubscriptions()
  }
  if (name === 'deliveries') loadDeliveries()
}

function openChannelCreate() {
  channelDialog.id = null
  channelDialog.visible = true
  channelForm.name = ''
  channelForm.type = 'WEBHOOK'
  channelForm.enabled = true
  channelForm.configJson = JSON.stringify({ url: '', signingSecret: '', timeoutSeconds: 30 }, null, 2)
}

function openChannelEdit(row) {
  channelDialog.id = row.id
  channelDialog.visible = true
  channelForm.name = row.name || ''
  channelForm.type = row.type || 'WEBHOOK'
  channelForm.enabled = !!row.enabled
  channelForm.configJson =
    typeof row.configJson === 'string' ? row.configJson : JSON.stringify(row.configJson || {}, null, 2)
}

function resetChannelForm() {
  channelFormRef.value?.resetFields?.()
}

function validateJson(str) {
  try {
    JSON.parse(str)
    return true
  } catch {
    return false
  }
}

async function submitChannel() {
  await channelFormRef.value?.validate?.()
  if (!validateJson(channelForm.configJson)) {
    ElMessage.error(t('IntegrationInvalidJson'))
    return
  }
  const body = {
    name: channelForm.name.trim(),
    type: channelForm.type,
    enabled: channelForm.enabled,
    configJson: channelForm.configJson.trim()
  }
  try {
    if (channelDialog.id) {
      await updateChannel(channelDialog.id, body)
      ElMessage.success(t('UpdateSuccess'))
    } else {
      await createChannel(body)
      ElMessage.success(t('CreateSuccess'))
    }
    channelDialog.visible = false
    await loadChannels()
    await loadChannelOptions()
  } catch {
    /* http interceptor */
  }
}

async function confirmDeleteChannel(row) {
  try {
    await ElMessageBox.confirm(t('DeleteConfirmLabel'), t('NoticeTitle'), { type: 'warning' })
    await deleteChannel(row.id)
    ElMessage.success(t('DeleteSuccess'))
    await loadChannels()
    await loadChannelOptions()
    if (subChannelId.value === row.id) {
      subChannelId.value = null
      subRows.value = []
    }
  } catch (e) {
    if (e !== 'cancel') {
      /* */
    }
  }
}

async function runChannelTest(row) {
  try {
    const res = await testChannel(row.id)
    if (res?.ok) {
      ElMessage.success(t('IntegrationTestOk'))
    } else {
      ElMessage.error(res?.error || t('IntegrationTestFail'))
    }
  } catch {
    /* */
  }
}

function openSubCreate() {
  if (!subChannelId.value) return
  subDialog.id = null
  subDialog.visible = true
  subForm.name = ''
  subForm.enabled = true
  subForm.eventTypes = []
  subForm.filterJson = ''
  subForm.transformJson = ''
}

function openSubEdit(row) {
  subDialog.id = row.id
  subDialog.visible = true
  subForm.name = row.name || ''
  subForm.enabled = !!row.enabled
  subForm.eventTypes = Array.isArray(row.eventTypes) ? [...row.eventTypes] : []
  subForm.filterJson = row.filterJson || ''
  subForm.transformJson = row.transformJson || ''
}

function resetSubForm() {
  subFormRef.value?.resetFields?.()
}

async function submitSub() {
  await subFormRef.value?.validate?.()
  if (subForm.filterJson && !validateJson(subForm.filterJson)) {
    ElMessage.error(t('IntegrationInvalidJson'))
    return
  }
  if (subForm.transformJson && !validateJson(subForm.transformJson)) {
    ElMessage.error(t('IntegrationInvalidJson'))
    return
  }
  const trimOrNull = (s) => {
    const x = (s || '').trim()
    return x === '' ? null : x
  }
  try {
    if (subDialog.id) {
      await updateSubscription(subDialog.id, {
        name: trimOrNull(subForm.name),
        enabled: subForm.enabled,
        eventTypes: subForm.eventTypes,
        filterJson: trimOrNull(subForm.filterJson),
        transformJson: trimOrNull(subForm.transformJson)
      })
      ElMessage.success(t('UpdateSuccess'))
    } else {
      await createSubscription({
        channelId: subChannelId.value,
        name: trimOrNull(subForm.name),
        enabled: subForm.enabled,
        eventTypes: subForm.eventTypes,
        filterJson: trimOrNull(subForm.filterJson),
        transformJson: trimOrNull(subForm.transformJson)
      })
      ElMessage.success(t('CreateSuccess'))
    }
    subDialog.visible = false
    await loadSubscriptions()
  } catch {
    /* */
  }
}

async function confirmDeleteSub(row) {
  try {
    await ElMessageBox.confirm(t('DeleteConfirmLabel'), t('NoticeTitle'), { type: 'warning' })
    await deleteSubscription(row.id)
    ElMessage.success(t('DeleteSuccess'))
    await loadSubscriptions()
  } catch (e) {
    if (e !== 'cancel') {
      /* */
    }
  }
}

async function runRetry(row) {
  try {
    await retryDelivery(row.id)
    ElMessage.success(t('IntegrationRetryQueued'))
    await loadDeliveries()
  } catch {
    /* */
  }
}

onMounted(() => {
  loadEventTypes()
  loadChannels()
  loadChannelOptions()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.sub-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.sub-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}
.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.tag-gap {
  margin-right: 4px;
  margin-bottom: 4px;
}
</style>

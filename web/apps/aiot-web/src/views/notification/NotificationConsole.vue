<template>
  <el-card shadow="always">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane :label="t('NotifyChannelsTab')" name="channels">
        <div class="toolbar">
          <el-button v-permission="'iot.notification.edit'" type="primary" :icon="Plus" @click="openChannelCreate">
            {{ t('NewButtonLabel') }}
          </el-button>
          <el-button :icon="Refresh" @click="loadChannels">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="channelRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('NotifyEmptyChannels')" />
          </template>
          <el-table-column prop="name" :label="t('NotifyChannelName')" min-width="140" />
          <el-table-column prop="type" :label="t('NotifyChannelType')" width="110" />
          <el-table-column :label="t('StatusLabel')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? t('Enabled') : t('Disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" :label="t('IntegrationUpdatedAt')" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('OperationsLabel')" width="360" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openChannelDetail(row)">{{ t('DetailLabel') }}</el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="openChannelEdit(row)">
                {{ t('EditLabel') }}
              </el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="toggleChannelEnabled(row)">
                {{ row.enabled ? t('Disable') : t('Enable') }}
              </el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="runChannelTest(row)">
                {{ t('NotifyTestChannel') }}
              </el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="confirmDeleteChannel(row)">
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

      <el-tab-pane :label="t('NotifySubscriptionsTab')" name="subscriptions">
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
            <el-option :label="t('AllLabel')" :value="null" />
            <el-option v-for="c in channelOptions" :key="c.id" :label="`${c.name} (#${c.id})`" :value="c.id" />
          </el-select>
          <el-button v-permission="'iot.notification.edit'" type="primary" :icon="Plus" @click="openSubCreate">
            {{ t('NewButtonLabel') }}
          </el-button>
          <el-button :icon="Refresh" @click="loadSubscriptions">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="subRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('NotifyEmptySubscriptions')" />
          </template>
          <el-table-column :label="t('IntegrationSelectChannel')" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ channelNameMap[row.channelId] || `#${row.channelId}` }}</template>
          </el-table-column>
          <el-table-column prop="name" :label="t('NameLabel')" min-width="120" show-overflow-tooltip />
          <el-table-column :label="t('IntegrationDeviceGroupsLabel')" min-width="120" prop="deviceGroupKey" />
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
          <el-table-column :label="t('OperationsLabel')" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openSubDetail(row)">{{ t('DetailLabel') }}</el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="openSubEdit(row)">{{
                t('EditLabel')
              }}</el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="toggleSubEnabled(row)">
                {{ row.enabled ? t('Disable') : t('Enable') }}
              </el-button>
              <el-button v-permission="'iot.notification.edit'" link type="primary" @click="confirmDeleteSub(row)">{{
                t('DeleteLabel')
              }}</el-button>
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
            @current-change="loadSubscriptions"
            @size-change="loadSubscriptions"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="t('NotifyDeliveriesTab')" name="deliveries">
        <div class="toolbar">
          <el-button :icon="Refresh" @click="loadDeliveries">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="deliveryRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('NotifyEmptyDeliveries')" />
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
                v-permission="'iot.notification.edit'"
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
        <el-form-item :label="t('NotifyChannelName')" prop="name">
          <el-input v-model="channelForm.name" />
        </el-form-item>
        <el-form-item :label="t('NotifyChannelType')" prop="type">
          <el-select v-model="channelForm.type" :disabled="!!channelDialog.id" style="width: 100%">
            <el-option label="DINGTALK" value="DINGTALK" />
            <el-option label="WECOM" value="WECOM" />
            <el-option label="FEISHU" value="FEISHU" />
            <el-option label="EMAIL" value="EMAIL" />
            <el-option label="SMS" value="SMS" />
          </el-select>
        </el-form-item>
        <template v-if="isBotType">
          <el-form-item :label="t('NotifyWebhookUrl')" required>
            <el-input v-model="botConfig.webhook" :placeholder="t('NotifyWebhookUrlPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('IntegrationWebhookTimeoutSeconds')">
            <el-input-number v-model="botConfig.timeoutSeconds" :min="1" :max="120" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-text type="info">{{ t('NotifyBotHint') }}</el-text>
          </el-form-item>
        </template>
        <template v-else-if="isEmailType">
          <el-form-item :label="t('NotifyEmailConfigJson')" prop="configJson">
            <el-input v-model="channelForm.configJson" type="textarea" :rows="10" :placeholder="t('NotifyEmailConfigPlaceholder')" />
          </el-form-item>
        </template>
        <template v-else-if="isSmsType">
          <el-form-item>
            <el-text type="info">{{ t('NotifySmsStubHint') }}</el-text>
          </el-form-item>
        </template>
        <el-form-item v-else :label="t('IntegrationConfigJson')" prop="configJson">
          <el-input v-model="channelForm.configJson" type="textarea" :rows="8" :placeholder="t('IntegrationConfigJsonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="channelDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="submitChannel">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="subDialog.visible" :title="subDialog.id ? t('EditTitle') : t('CreateTitle')" width="680px" destroy-on-close @closed="resetSubForm">
      <el-form ref="subFormRef" :model="subForm" :rules="subRules" label-position="top">
        <el-form-item :label="t('IntegrationSelectChannel')" prop="channelId">
          <el-select v-model="subForm.channelId" filterable style="width: 100%">
            <el-option v-for="c in channelOptions" :key="c.id" :label="`${c.name} (#${c.id})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('NameLabel')" prop="name">
          <el-input v-model="subForm.name" :placeholder="t('IntegrationSubscriptionNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('IntegrationDeviceGroupsLabel')" prop="deviceGroupKey">
          <el-select v-model="subForm.deviceGroupKey" clearable filterable style="width: 100%">
            <el-option
              v-for="g in deviceGroupOptions"
              :key="g.groupKey"
              :label="g.name ? `${g.name} (#${g.groupKey})` : `#${g.groupKey}`"
              :value="g.groupKey"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('IntegrationEventTypes')" prop="eventTypes">
          <el-select v-model="subForm.eventTypes" multiple filterable style="width: 100%">
            <el-option v-for="et in eventTypeOptions" :key="et" :label="et" :value="et" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('NotifyTemplateTitle')">
          <el-input v-model="subForm.templateTitle" :placeholder="t('NotifyTemplateTitlePh')" />
        </el-form-item>
        <el-form-item :label="t('NotifyTemplateBody')">
          <el-input v-model="subForm.templateBody" type="textarea" :rows="4" :placeholder="t('NotifyTemplateBodyPh')" />
        </el-form-item>
        <el-form-item>
          <el-text type="info">{{ t('NotifyTemplateMustacheHint') }}</el-text>
        </el-form-item>
        <el-form-item :label="t('IntegrationFilterJson')">
          <el-input v-model="subForm.filterJson" type="textarea" :rows="3" :placeholder="t('IntegrationFilterJsonPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="submitSub">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="subDetailDialog.visible" :title="t('DetailLabel')" width="680px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="t('IntegrationSelectChannel')">
          <el-input :model-value="channelNameMap[subDetail.channelId] || `#${subDetail.channelId || ''}`" readonly />
        </el-form-item>
        <el-form-item :label="t('NameLabel')"><el-input :model-value="subDetail.name" readonly /></el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-input :model-value="subDetail.enabled ? t('Enabled') : t('Disabled')" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationDeviceGroupsLabel')">
          <el-input :model-value="subDetail.deviceGroupKey || ''" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationEventTypes')">
          <el-input :model-value="(subDetail.eventTypes || []).join(', ')" readonly />
        </el-form-item>
        <el-form-item :label="t('NotifyTemplateJson')">
          <el-input :model-value="subDetail.templateJson || ''" type="textarea" :rows="6" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationFilterJson')">
          <el-input :model-value="subDetail.filterJson || ''" type="textarea" :rows="3" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subDetailDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="channelDetailDialog.visible" :title="t('DetailLabel')" width="560px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="t('NotifyChannelName')"><el-input :model-value="channelDetail.name" readonly /></el-form-item>
        <el-form-item :label="t('NotifyChannelType')"><el-input :model-value="channelDetail.type" readonly /></el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-input :model-value="channelDetail.enabled ? t('Enabled') : t('Disabled')" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationConfigJson')">
          <el-input :model-value="channelDetail.configJson" type="textarea" :rows="8" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="channelDetailDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createChannel,
  createSubscription,
  deleteChannel,
  deleteSubscription,
  getChannel,
  getSubscription,
  listChannels,
  listDeliveries,
  listEventTypes,
  listSubscriptions,
  retryDelivery,
  testChannel,
  updateChannel,
  updateSubscription
} from '@/views/notification/AppNotificationApi'
import { listDeviceGroups } from '@/views/device-groups/DeviceGroupApi'

const { t } = useI18n()
const activeTab = ref('channels')
const eventTypeOptions = ref([])

const channelRows = ref([])
const channelPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const channelOptions = ref([])
const channelNameMap = computed(() => {
  const map = {}
  for (const c of channelOptions.value) map[c.id] = c.name
  return map
})

const subChannelId = ref(null)
const subRows = ref([])
const subPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const deliveryRows = ref([])
const deliveryPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const channelDialog = reactive({ visible: false, id: null })
const channelDetailDialog = reactive({ visible: false })
const channelDetail = reactive({ name: '', type: '', enabled: false, configJson: '' })
const channelFormRef = ref(null)
const channelForm = reactive({ name: '', type: 'DINGTALK', enabled: true, configJson: '{}' })
const botConfig = reactive({ webhook: '', timeoutSeconds: 30 })

const subDialog = reactive({ visible: false, id: null })
const subDetailDialog = reactive({ visible: false })
const subDetail = reactive({
  channelId: null,
  name: '',
  enabled: false,
  eventTypes: [],
  filterJson: '',
  deviceGroupKey: '',
  templateJson: ''
})
const subFormRef = ref(null)
const subForm = reactive({
  channelId: null,
  name: '',
  enabled: true,
  eventTypes: [],
  filterJson: '',
  deviceGroupKey: null,
  templateTitle: '',
  templateBody: ''
})

const deviceGroupOptions = ref([])

const isBotType = computed(() => ['DINGTALK', 'WECOM', 'FEISHU'].includes(String(channelForm.type || '').toUpperCase()))
const isEmailType = computed(() => String(channelForm.type || '').toUpperCase() === 'EMAIL')
const isSmsType = computed(() => String(channelForm.type || '').toUpperCase() === 'SMS')

const channelRules = computed(() => ({
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  type: [{ required: true, message: t('IntegrationTypeRequired'), trigger: 'change' }],
  configJson: [
    {
      validator: (_, value, callback) => {
        if (isBotType.value || isSmsType.value) {
          callback()
          return
        }
        if (isEmailType.value) {
          if (!value || !String(value).trim()) {
            callback(new Error(t('IntegrationConfigRequired')))
            return
          }
          if (!validateJson(value)) {
            callback(new Error(t('IntegrationInvalidJson')))
            return
          }
          callback()
          return
        }
        if (!value || !String(value).trim()) {
          callback(new Error(t('IntegrationConfigRequired')))
          return
        }
        if (!validateJson(value)) {
          callback(new Error(t('IntegrationInvalidJson')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}))

const subRules = computed(() => ({
  channelId: [{ required: true, message: t('IntegrationSelectChannel'), trigger: 'change' }],
  eventTypes: [{ required: true, message: t('IntegrationEventTypesRequired'), trigger: 'change' }],
  deviceGroupKey: [{ required: true, message: t('NotifyDeviceGroupRequired'), trigger: 'change' }]
}))

function parsePage(raw) {
  const records = Array.isArray(raw?.records) ? raw.records : []
  const total = Number.isFinite(Number(raw?.total)) ? Number(raw.total) : records.length
  return { records, total }
}

function validateJson(str) {
  try {
    JSON.parse(str)
    return true
  } catch {
    return false
  }
}

function normalizeTimeoutSeconds(value) {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) return 30
  return Math.min(Math.floor(n), 120)
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
    const { records } = parsePage(raw)
    channelOptions.value = records
  } catch {
    channelOptions.value = []
  }
}

async function loadChannels() {
  try {
    const raw = await listChannels({ pageNum: channelPage.pageNum, pageSize: channelPage.pageSize })
    const { records, total } = parsePage(raw)
    channelRows.value = records
    channelPage.total = total
  } catch {
    channelRows.value = []
  }
}

async function loadSubscriptions() {
  try {
    const raw = await listSubscriptions({
      channelId: subChannelId.value,
      pageNum: subPage.pageNum,
      pageSize: subPage.pageSize
    })
    const { records, total } = parsePage(raw)
    subRows.value = records
    subPage.total = total
  } catch {
    subRows.value = []
  }
}

function normalizeDeviceGroupListResponse(raw) {
  if (Array.isArray(raw)) return raw
  const records = Array.isArray(raw?.records) ? raw.records : []
  return records
}

async function loadDeviceGroups() {
  try {
    const raw = await listDeviceGroups({ pageNum: 1, pageSize: 1000 })
    deviceGroupOptions.value = normalizeDeviceGroupListResponse(raw)
  } catch {
    deviceGroupOptions.value = []
  }
}

async function loadDeliveries() {
  try {
    const raw = await listDeliveries({ pageNum: deliveryPage.pageNum, pageSize: deliveryPage.pageSize })
    const { records, total } = parsePage(raw)
    deliveryRows.value = records
    deliveryPage.total = total
  } catch {
    deliveryRows.value = []
  }
}

function onTabChange(name) {
  if (name === 'subscriptions') {
    loadChannelOptions()
    loadDeviceGroups()
    loadSubscriptions()
  }
  if (name === 'deliveries') loadDeliveries()
}

function syncBotFromJson(jsonText) {
  try {
    const cfg = JSON.parse(jsonText || '{}')
    botConfig.webhook = typeof cfg.webhook === 'string' ? cfg.webhook : typeof cfg.url === 'string' ? cfg.url : ''
    botConfig.timeoutSeconds = normalizeTimeoutSeconds(cfg.timeoutSeconds)
  } catch {
    botConfig.webhook = ''
    botConfig.timeoutSeconds = 30
  }
}

function openChannelCreate() {
  channelDialog.id = null
  channelDialog.visible = true
  channelForm.name = ''
  channelForm.type = 'DINGTALK'
  channelForm.enabled = true
  botConfig.webhook = ''
  botConfig.timeoutSeconds = 30
  channelForm.configJson = JSON.stringify({ webhook: '', timeoutSeconds: 30 }, null, 2)
}

async function openChannelEdit(row) {
  try {
    const detail = await getChannel(row.id)
    channelDialog.id = detail.id
    channelDialog.visible = true
    channelForm.name = detail.name || ''
    channelForm.type = detail.type || 'DINGTALK'
    channelForm.enabled = !!detail.enabled
    channelForm.configJson =
      typeof detail.configJson === 'string' ? detail.configJson : JSON.stringify(detail.configJson || {}, null, 2)
    if (isBotType.value) syncBotFromJson(channelForm.configJson)
  } catch {
    /* */
  }
}

function openChannelDetail(row) {
  channelDetail.name = row.name || ''
  channelDetail.type = row.type || ''
  channelDetail.enabled = !!row.enabled
  channelDetail.configJson =
    typeof row.configJson === 'string' ? row.configJson : JSON.stringify(row.configJson || {}, null, 2)
  channelDetailDialog.visible = true
}

function resetChannelForm() {
  channelFormRef.value?.resetFields?.()
}

watch(
  () => channelForm.type,
  (type) => {
    const u = String(type || '').toUpperCase()
    if (['DINGTALK', 'WECOM', 'FEISHU'].includes(u)) {
      syncBotFromJson(channelForm.configJson)
    }
    if (u === 'SMS') {
      channelForm.configJson = '{}'
    }
  }
)

async function submitChannel() {
  await channelFormRef.value?.validate?.()
  if (isBotType.value) {
    if (!botConfig.webhook || !botConfig.webhook.trim()) {
      ElMessage.error(t('NotifyWebhookRequired'))
      return
    }
    botConfig.timeoutSeconds = normalizeTimeoutSeconds(botConfig.timeoutSeconds)
    channelForm.configJson = JSON.stringify(
      { webhook: botConfig.webhook.trim(), timeoutSeconds: botConfig.timeoutSeconds },
      null,
      2
    )
  } else if (isSmsType.value) {
    channelForm.configJson = '{}'
  } else if (!validateJson(channelForm.configJson)) {
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
    /* */
  }
}

async function toggleChannelEnabled(row) {
  try {
    const detail = await getChannel(row.id)
    await updateChannel(row.id, {
      name: detail.name,
      type: detail.type,
      enabled: !row.enabled,
      configJson: detail.configJson
    })
    ElMessage.success(t('UpdateSuccess'))
    await loadChannels()
    await loadChannelOptions()
  } catch {
    /* */
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
      ElMessage.success(t('NotifyTestOk'))
    } else {
      ElMessage.error(res?.error || t('NotifyTestFail'))
    }
  } catch {
    /* */
  }
}

function parseTemplateFields(templateJson) {
  let title = ''
  let body = ''
  try {
    if (templateJson && String(templateJson).trim()) {
      const o = JSON.parse(templateJson)
      title = typeof o.title === 'string' ? o.title : ''
      body = typeof o.body === 'string' ? o.body : ''
    }
  } catch {
    /* */
  }
  return { title, body }
}

function openSubCreate() {
  subDialog.id = null
  subDialog.visible = true
  subForm.channelId = subChannelId.value
  subForm.name = ''
  subForm.enabled = true
  subForm.eventTypes = []
  subForm.filterJson = ''
  subForm.deviceGroupKey = null
  subForm.templateTitle = '{{eventType}} / {{subject.productKey}}'
  subForm.templateBody = '{{eventType}}\n{{subject.deviceKey}}\n{{data}}'
}

async function openSubEdit(row) {
  try {
    const detail = await getSubscription(row.id)
    subDialog.id = detail.id
    subDialog.visible = true
    subForm.channelId = detail.channelId
    subForm.name = detail.name || ''
    subForm.enabled = !!detail.enabled
    subForm.eventTypes = Array.isArray(detail.eventTypes) ? [...detail.eventTypes] : []
    subForm.filterJson = detail.filterJson || ''
    subForm.deviceGroupKey = detail.deviceGroupKey || null
    const tf = parseTemplateFields(detail.templateJson)
    subForm.templateTitle = tf.title
    subForm.templateBody = tf.body
  } catch {
    /* */
  }
}

async function openSubDetail(row) {
  try {
    const detail = await getSubscription(row.id)
    subDetail.channelId = detail.channelId
    subDetail.name = detail.name || ''
    subDetail.enabled = !!detail.enabled
    subDetail.eventTypes = Array.isArray(detail.eventTypes) ? [...detail.eventTypes] : []
    subDetail.filterJson = detail.filterJson || ''
    subDetail.deviceGroupKey = detail.deviceGroupKey || ''
    subDetail.templateJson =
      typeof detail.templateJson === 'string' ? detail.templateJson : JSON.stringify(detail.templateJson || {}, null, 2)
    subDetailDialog.visible = true
  } catch {
    /* */
  }
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
  const trimOrNull = (s) => {
    const x = (s || '').trim()
    return x === '' ? null : x
  }
  const title = (subForm.templateTitle || '').trim()
  const body = (subForm.templateBody || '').trim()
  if (!title && !body) {
    ElMessage.error(t('NotifyTemplateRequired'))
    return
  }
  const templateJson = JSON.stringify({ title: subForm.templateTitle || '', body: subForm.templateBody || '' })
  try {
    const payload = {
      channelId: subForm.channelId,
      name: trimOrNull(subForm.name),
      enabled: subForm.enabled,
      deviceGroupKey: subForm.deviceGroupKey,
      eventTypes: subForm.eventTypes,
      filterJson: trimOrNull(subForm.filterJson),
      templateJson
    }
    if (subDialog.id) {
      await updateSubscription(subDialog.id, payload)
      ElMessage.success(t('UpdateSuccess'))
    } else {
      await createSubscription(payload)
      ElMessage.success(t('CreateSuccess'))
    }
    subDialog.visible = false
    await loadSubscriptions()
  } catch {
    /* */
  }
}

async function toggleSubEnabled(row) {
  try {
    const detail = await getSubscription(row.id)
    await updateSubscription(row.id, {
      channelId: detail.channelId,
      name: detail.name,
      enabled: !row.enabled,
      deviceGroupKey: detail.deviceGroupKey,
      eventTypes: detail.eventTypes,
      filterJson: detail.filterJson,
      templateJson: detail.templateJson
    })
    ElMessage.success(t('UpdateSuccess'))
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

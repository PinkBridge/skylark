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
          <el-table-column :label="t('OperationsLabel')" width="360" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openChannelDetail(row)">
                {{ t('DetailLabel') }}
              </el-button>
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="openChannelEdit(row)">
                {{ t('EditLabel') }}
              </el-button>
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="toggleChannelEnabled(row)">
                {{ row.enabled ? t('Disable') : t('Enable') }}
              </el-button>
              <el-button
                v-permission="'iot.integration.edit'"
                link
                type="primary"
                :disabled="!['WEBHOOK', 'MQTT'].includes(String(row.type).toUpperCase())"
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
            <el-option :label="t('AllLabel')" :value="null" />
            <el-option v-for="c in channelOptions" :key="c.id" :label="`${c.name} (#${c.id})`" :value="c.id" />
          </el-select>
          <el-button v-permission="'iot.integration.edit'" type="primary" :icon="Plus" @click="openSubCreate">
            {{ t('NewButtonLabel') }}
          </el-button>
          <el-button :icon="Refresh" @click="loadSubscriptions">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
        <el-table :data="subRows" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="t('IntegrationEmptySubscriptions')" />
          </template>
          <el-table-column :label="t('IntegrationSelectChannel')" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ channelNameMap[row.channelId] || `#${row.channelId}` }}
            </template>
          </el-table-column>
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
          <el-table-column :label="t('OperationsLabel')" width="240" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openSubDetail(row)">{{ t('DetailLabel') }}</el-button>
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="openSubEdit(row)">{{ t('EditLabel') }}</el-button>
              <el-button v-permission="'iot.integration.edit'" link type="primary" @click="toggleSubEnabled(row)">
                {{ row.enabled ? t('Disable') : t('Enable') }}
              </el-button>
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
          </el-select>
        </el-form-item>
        <template v-if="isWebhookType">
          <el-form-item :label="t('IntegrationWebhookUrl')" required>
            <el-input v-model="webhookConfig.url" :placeholder="t('IntegrationWebhookUrlPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('IntegrationWebhookSigningSecret')">
            <el-input
              v-model="webhookConfig.signingSecret"
              :type="signingSecretVisible ? 'text' : 'password'"
              :placeholder="t('IntegrationWebhookSigningSecretPlaceholder')"
            >
              <template #suffix>
                <el-icon class="secret-toggle-icon" @click="signingSecretVisible = !signingSecretVisible">
                  <View v-if="!signingSecretVisible" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item :label="t('IntegrationWebhookTimeoutSeconds')">
            <el-input-number v-model="webhookConfig.timeoutSeconds" :min="1" :max="120" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item>
            <el-text type="info">{{ t('IntegrationWebhookRequestHint') }}</el-text>
          </el-form-item>
        </template>
        <template v-else-if="isMqttType">
          <el-form-item :label="t('IntegrationMqttBrokerUrl')" required>
            <el-input v-model="mqttConfig.brokerUrl" :placeholder="t('IntegrationMqttBrokerUrlPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('IntegrationMqttTopic')" required>
            <el-input v-model="mqttConfig.topic" :placeholder="t('IntegrationMqttTopicPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('IntegrationMqttClientId')">
            <el-input v-model="mqttConfig.clientId" :placeholder="t('IntegrationMqttClientIdPlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('IntegrationMqttUsername')">
            <el-input v-model="mqttConfig.username" :placeholder="t('IntegrationMqttUsernamePlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('IntegrationMqttPassword')">
            <el-input
              v-model="mqttConfig.password"
              :type="mqttPasswordVisible ? 'text' : 'password'"
              :placeholder="t('IntegrationMqttPasswordPlaceholder')"
            >
              <template #suffix>
                <el-icon class="secret-toggle-icon" @click="mqttPasswordVisible = !mqttPasswordVisible">
                  <View v-if="!mqttPasswordVisible" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item :label="t('IntegrationMqttQos')">
            <el-select v-model="mqttConfig.qos" style="width: 100%">
              <el-option :label="'0'" :value="0" />
              <el-option :label="'1'" :value="1" />
              <el-option :label="'2'" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-text type="info">{{ t('IntegrationMqttRequestHint') }}</el-text>
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

    <el-dialog v-model="subDialog.visible" :title="subDialog.id ? t('EditTitle') : t('CreateTitle')" width="640px" destroy-on-close @closed="resetSubForm">
      <el-form ref="subFormRef" :model="subForm" :rules="subRules" label-position="top">
        <el-form-item :label="t('IntegrationSelectChannel')" prop="channelId">
          <el-select v-model="subForm.channelId" filterable style="width: 100%">
            <el-option v-for="c in channelOptions" :key="c.id" :label="`${c.name} (#${c.id})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('NameLabel')" prop="name">
          <el-input v-model="subForm.name" :placeholder="t('IntegrationSubscriptionNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('IntegrationEventTypes')" prop="eventTypes">
          <el-select v-model="subForm.eventTypes" multiple filterable style="width: 100%">
            <el-option v-for="et in eventTypeOptions" :key="et" :label="et" :value="et" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('IntegrationDeviceGroupsLabel')">
          <el-select v-model="subForm.deviceGroupKey" clearable filterable style="width: 100%">
            <el-option v-for="g in deviceGroupOptions" :key="g.groupKey" :label="g.name ? `${g.name} (#${g.groupKey})` : `#${g.groupKey}`" :value="g.groupKey" />
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

    <el-dialog v-model="subDetailDialog.visible" :title="t('DetailLabel')" width="640px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="t('IntegrationSelectChannel')">
          <el-input :model-value="channelNameMap[subDetail.channelId] || `#${subDetail.channelId || ''}`" readonly />
        </el-form-item>
        <el-form-item :label="t('NameLabel')">
          <el-input :model-value="subDetail.name" readonly />
        </el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-input :model-value="subDetail.enabled ? t('Enabled') : t('Disabled')" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationEventTypes')">
          <el-input :model-value="(subDetail.eventTypes || []).join(', ')" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationDeviceGroupsLabel')">
          <el-input :model-value="subDetail.deviceGroupKey || ''" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationFilterJson')">
          <el-input :model-value="subDetail.filterJson || ''" type="textarea" :rows="3" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationTransformJson')">
          <el-input :model-value="subDetail.transformJson || ''" type="textarea" :rows="3" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subDetailDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="channelDetailDialog.visible" :title="t('DetailLabel')" width="560px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="t('IntegrationChannelName')">
          <el-input :model-value="channelDetail.name" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationChannelType')">
          <el-input :model-value="channelDetail.type" readonly />
        </el-form-item>
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
import { Hide, Plus, Refresh, View } from '@element-plus/icons-vue'
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
} from '@/views/integration/AppIntegrationApi'
import { listDeviceGroups } from '@/views/device-groups/DeviceGroupApi'

const { t } = useI18n()
const activeTab = ref('channels')
const eventTypeOptions = ref([])

const channelRows = ref([])
const channelPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const channelOptions = ref([])
const channelNameMap = computed(() => {
  const map = {}
  for (const c of channelOptions.value) {
    map[c.id] = c.name
  }
  return map
})

const subChannelId = ref(null)
const subRows = ref([])
const subPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const deliveryRows = ref([])
const deliveryPage = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const channelDialog = reactive({ visible: false, id: null })
const channelDetailDialog = reactive({ visible: false })
const channelDetail = reactive({
  name: '',
  type: '',
  enabled: false,
  configJson: ''
})
const channelFormRef = ref(null)
const channelForm = reactive({
  name: '',
  type: 'WEBHOOK',
  enabled: true,
  configJson: ''
})
const webhookConfig = reactive({ url: '', signingSecret: '', timeoutSeconds: 30 })
const signingSecretVisible = ref(false)
const mqttConfig = reactive({
  brokerUrl: '',
  topic: '',
  clientId: '',
  username: '',
  password: '',
  qos: 0
})
const mqttPasswordVisible = ref(false)

const subDialog = reactive({ visible: false, id: null })
const subDetailDialog = reactive({ visible: false })
const subDetail = reactive({
  channelId: null,
  name: '',
  enabled: false,
  eventTypes: [],
  filterJson: '',
  deviceGroupKey: null,
  transformJson: ''
})
const subFormRef = ref(null)
const subForm = reactive({
  channelId: null,
  name: '',
  enabled: true,
  eventTypes: [],
  filterJson: '',
  deviceGroupKey: null,
  transformJson: ''
})

const isWebhookType = computed(() => String(channelForm.type || '').toUpperCase() === 'WEBHOOK')
const isMqttType = computed(() => String(channelForm.type || '').toUpperCase() === 'MQTT')

const channelRules = computed(() => ({
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  type: [{ required: true, message: t('IntegrationTypeRequired'), trigger: 'change' }],
  configJson: [{
    validator: (_, value, callback) => {
      if (isWebhookType.value) {
        callback()
        return
      }
      if (isMqttType.value) {
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
  }]
}))

const subRules = computed(() => ({
  channelId: [{ required: true, message: t('IntegrationSelectChannel'), trigger: 'change' }],
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

const deviceGroupOptions = ref([])

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
    loadDeviceGroups()
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
  webhookConfig.url = ''
  webhookConfig.signingSecret = ''
  webhookConfig.timeoutSeconds = 30
  signingSecretVisible.value = false
  mqttConfig.brokerUrl = ''
  mqttConfig.topic = ''
  mqttConfig.clientId = ''
  mqttConfig.username = ''
  mqttConfig.password = ''
  mqttConfig.qos = 0
  mqttPasswordVisible.value = false
  channelForm.configJson = JSON.stringify(
    { url: webhookConfig.url, signingSecret: webhookConfig.signingSecret, timeoutSeconds: webhookConfig.timeoutSeconds },
    null,
    2
  )
}

async function openChannelEdit(row) {
  try {
    const detail = await getChannel(row.id)
    channelDialog.id = detail.id
    channelDialog.visible = true
    channelForm.name = detail.name || ''
    channelForm.type = detail.type || 'WEBHOOK'
    channelForm.enabled = !!detail.enabled
    channelForm.configJson =
      typeof detail.configJson === 'string' ? detail.configJson : JSON.stringify(detail.configJson || {}, null, 2)
    syncChannelConfigFromJson()
    signingSecretVisible.value = false
    mqttPasswordVisible.value = false
  } catch {
    /* handled by interceptor */
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

function normalizeTimeoutSeconds(value) {
  const n = Number(value)
  if (!Number.isFinite(n) || n <= 0) {
    return 30
  }
  return Math.min(Math.floor(n), 120)
}

function syncWebhookConfigFromJson(jsonText) {
  try {
    const cfg = JSON.parse(jsonText || '{}')
    webhookConfig.url = typeof cfg.url === 'string' ? cfg.url : ''
    webhookConfig.signingSecret = typeof cfg.signingSecret === 'string' ? cfg.signingSecret : ''
    webhookConfig.timeoutSeconds = normalizeTimeoutSeconds(cfg.timeoutSeconds)
  } catch {
    webhookConfig.url = ''
    webhookConfig.signingSecret = ''
    webhookConfig.timeoutSeconds = 30
  }
}

function normalizeMqttQos(value) {
  const n = Number(value)
  if (!Number.isFinite(n) || n < 0) return 0
  return Math.min(Math.floor(n), 2)
}

function syncMqttConfigFromJson(jsonText) {
  try {
    const cfg = JSON.parse(jsonText || '{}')
    mqttConfig.brokerUrl = typeof cfg.brokerUrl === 'string' ? cfg.brokerUrl : (typeof cfg.url === 'string' ? cfg.url : '')
    mqttConfig.topic = typeof cfg.topic === 'string' ? cfg.topic : ''
    mqttConfig.clientId = typeof cfg.clientId === 'string' ? cfg.clientId : ''
    mqttConfig.username = typeof cfg.username === 'string' ? cfg.username : ''
    mqttConfig.password = typeof cfg.password === 'string' ? cfg.password : ''
    mqttConfig.qos = normalizeMqttQos(cfg.qos)
  } catch {
    mqttConfig.brokerUrl = ''
    mqttConfig.topic = ''
    mqttConfig.clientId = ''
    mqttConfig.username = ''
    mqttConfig.password = ''
    mqttConfig.qos = 0
  }
}

function syncChannelConfigFromJson() {
  if (isWebhookType.value) {
    syncWebhookConfigFromJson(channelForm.configJson)
    return
  }
  if (isMqttType.value) {
    syncMqttConfigFromJson(channelForm.configJson)
  }
}

watch(
  () => channelForm.type,
  (type) => {
    if (String(type || '').toUpperCase() === 'WEBHOOK' || String(type || '').toUpperCase() === 'MQTT') {
      syncChannelConfigFromJson()
    }
  }
)

function validateJson(str) {
  try {
    JSON.parse(str)
    return true
  } catch {
    return false
  }
}

function parseDeviceGroupKeyFromFilter(filterJson) {
  try {
    if (!filterJson || !String(filterJson).trim()) return null
    const obj = JSON.parse(filterJson)
    if (!obj || typeof obj !== 'object') return null
    const gk = obj.deviceGroupKey
    return typeof gk === 'string' && gk.trim() ? gk.trim() : null
  } catch {
    return null
  }
}

async function submitChannel() {
  await channelFormRef.value?.validate?.()
  if (isWebhookType.value) {
    if (!webhookConfig.url || !webhookConfig.url.trim()) {
      ElMessage.error(t('IntegrationWebhookUrlRequired'))
      return
    }
    webhookConfig.timeoutSeconds = normalizeTimeoutSeconds(webhookConfig.timeoutSeconds)
    channelForm.configJson = JSON.stringify(
      {
        url: webhookConfig.url.trim(),
        signingSecret: (webhookConfig.signingSecret || '').trim(),
        timeoutSeconds: webhookConfig.timeoutSeconds
      },
      null,
      2
    )
  } else if (isMqttType.value) {
    if (!mqttConfig.brokerUrl || !mqttConfig.brokerUrl.trim()) {
      ElMessage.error(t('IntegrationMqttBrokerUrlRequired'))
      return
    }
    if (!mqttConfig.topic || !mqttConfig.topic.trim()) {
      ElMessage.error(t('IntegrationMqttTopicRequired'))
      return
    }
    mqttConfig.qos = normalizeMqttQos(mqttConfig.qos)
    channelForm.configJson = JSON.stringify(
      {
        brokerUrl: mqttConfig.brokerUrl.trim(),
        topic: mqttConfig.topic.trim(),
        clientId: (mqttConfig.clientId || '').trim(),
        username: (mqttConfig.username || '').trim(),
        password: (mqttConfig.password || '').trim(),
        qos: mqttConfig.qos
      },
      null,
      2
    )
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
    /* http interceptor */
  }
}

async function toggleChannelEnabled(row) {
  try {
    const detail = await getChannel(row.id)
    await updateChannel(row.id, {
      name: detail.name,
      enabled: !row.enabled,
      configJson: detail.configJson
    })
    ElMessage.success(t('UpdateSuccess'))
    await loadChannels()
    await loadChannelOptions()
  } catch {
    /* handled by interceptor */
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
  subDialog.id = null
  subDialog.visible = true
  subForm.channelId = subChannelId.value
  subForm.name = ''
  subForm.enabled = true
  subForm.eventTypes = []
  subForm.filterJson = ''
  subForm.deviceGroupKey = null
  subForm.transformJson = ''
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
    subForm.deviceGroupKey = parseDeviceGroupKeyFromFilter(subForm.filterJson)
    subForm.transformJson = detail.transformJson || ''
  } catch {
    /* handled by interceptor */
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
    subDetail.deviceGroupKey = parseDeviceGroupKeyFromFilter(subDetail.filterJson)
    subDetail.transformJson = detail.transformJson || ''
    subDetailDialog.visible = true
  } catch {
    /* handled by interceptor */
  }
}

function resetSubForm() {
  subFormRef.value?.resetFields?.()
  subForm.deviceGroupKey = null
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
    // Merge device group selection into filterJson.deviceGroupKey
    let mergedFilterObj = {}
    if (trimOrNull(subForm.filterJson)) {
      try {
        mergedFilterObj = JSON.parse(trimOrNull(subForm.filterJson)) || {}
      } catch {
        mergedFilterObj = {}
      }
    }
    if (subForm.deviceGroupKey) {
      mergedFilterObj.deviceGroupKey = subForm.deviceGroupKey
    } else {
      delete mergedFilterObj.deviceGroupKey
    }
    const mergedFilterJson = Object.keys(mergedFilterObj).length ? JSON.stringify(mergedFilterObj, null, 2) : null

    if (subDialog.id) {
      await updateSubscription(subDialog.id, {
        name: trimOrNull(subForm.name),
        enabled: subForm.enabled,
        eventTypes: subForm.eventTypes,
        filterJson: mergedFilterJson,
        transformJson: trimOrNull(subForm.transformJson)
      })
      ElMessage.success(t('UpdateSuccess'))
    } else {
      await createSubscription({
        channelId: subForm.channelId,
        name: trimOrNull(subForm.name),
        enabled: subForm.enabled,
        eventTypes: subForm.eventTypes,
        filterJson: mergedFilterJson,
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

async function toggleSubEnabled(row) {
  try {
    const detail = await getSubscription(row.id)
    await updateSubscription(row.id, {
      name: detail.name,
      enabled: !row.enabled,
      eventTypes: detail.eventTypes,
      filterJson: detail.filterJson,
      transformJson: detail.transformJson
    })
    ElMessage.success(t('UpdateSuccess'))
    await loadSubscriptions()
  } catch {
    /* handled by interceptor */
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
.secret-toggle-icon {
  cursor: pointer;
}
</style>

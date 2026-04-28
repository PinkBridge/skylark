<template>
  <div class="device-detail-page">
    <el-card shadow="always">
      <div class="page-toolbar">
        <div class="page-title-wrap">
          <el-button class="back-button" text :icon="ArrowLeft" @click="router.push('/iot/devices')" />
          <div class="page-title">{{ $t('DeviceDetailTitle') }}</div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">{{ $t('BasicInfoTitle') }}</div>
        <el-descriptions :column="3" border class="detail-descriptions">
          <el-descriptions-item :label="$t('DeviceNameLabel')">{{ detail.deviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('DeviceKeyLabel')">
            <span class="secret-row">
              <span class="secret-text">{{ detail.deviceKey || '-' }}</span>
              <el-button
                v-if="detail.deviceKey"
                class="secret-icon-btn"
                link
                type="primary"
                :icon="DocumentCopy"
                :title="$t('CopyLabel')"
                @click="copyText(detail.deviceKey)"
              />
            </span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('SecretLabel')">
            <span class="secret-row">
              <span class="secret-text">{{ displayDeviceSecret }}</span>
              <span v-if="detail.secret" class="secret-actions">
                <el-button
                  class="secret-icon-btn"
                  link
                  type="primary"
                  :icon="deviceSecretVisible ? Hide : View"
                  @click="deviceSecretVisible = !deviceSecretVisible"
                />
                <el-button
                  class="secret-icon-btn"
                  link
                  type="primary"
                  :icon="DocumentCopy"
                  :title="$t('CopyLabel')"
                  @click="copyText(detail.secret)"
                />
                <el-button
                  class="secret-icon-btn"
                  link
                  type="primary"
                  :icon="RefreshRight"
                  :title="$t('ResetSecretButtonText')"
                  @click="resetSecret"
                />
              </span>
            </span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('ProductKeyLabel')">{{ detail.productKey || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('DeviceTypeLabel')">{{ detail.deviceType || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ProtocolTypeLabel')">{{ detail.protocolType || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ProtocolVersionLabel')">{{ detail.protocolVersion || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('StatusLabel')">
            <el-tag :type="detail.status === 'enabled' ? 'success' : 'danger'">
              {{ detail.status === 'enabled' ? $t('Enabled') : $t('Disabled') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('DeviceAddressLabel')" :span="3">{{ detail.address || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="detail-section">
        <div class="section-title">{{ $t('DeviceCurrentPropertiesTitle') }}</div>
        <div v-if="currentPropertyCards.length" class="property-card-grid">
          <el-card v-for="item in currentPropertyCards" :key="item.propertyIdentifier" shadow="hover" class="property-card">
            <div class="property-card-header">
              <div class="property-card-label">{{ item.propertyIdentifier }}</div>
              <div class="property-card-actions">
                <el-button
                  link
                  type="primary"
                  class="property-refresh-button"
                  :loading="isPropertyRefreshing(item.propertyIdentifier)"
                  @click="refreshCurrentPropertyValue(item.propertyIdentifier)"
                >
                  {{ $t('RefreshButtonLabel') }}
                </el-button>
                <el-button
                  v-if="item.isNumeric"
                  link
                  type="primary"
                  class="property-chart-button"
                  @click="openPropertyChart(item.propertyIdentifier)"
                >
                  {{ $t('DevicePropertyChartAction') }}
                </el-button>
              </div>
            </div>
            <div class="property-card-value">{{ item.propertyValue || '-' }}</div>
            <div class="property-card-time">{{ formatTime(item.createdAt, item.deviceTimestamp) }}</div>
          </el-card>
        </div>
        <el-empty v-else :description="$t('DeviceRecordsEmpty')" />
      </div>

      <div class="detail-section">
        <div class="section-title">{{ $t('ProductDataChannelTitle') }}</div>
        <el-table :data="dataChannels" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="$t('ProductDataChannelNoData')" />
          </template>
          <el-table-column prop="action" :label="$t('ProductDataChannelActionLabel')" width="120" />
          <el-table-column :label="$t('ProductDataChannelTopicLabel')" min-width="340">
            <template #default="{ row }">
              <span class="topic-pattern-cell">
                <span class="topic-pattern-text" :title="row.topicPattern || '-'">{{ row.topicPattern || '-' }}</span>
                <el-button
                  v-if="row.topicPattern"
                  class="secret-icon-btn"
                  link
                  type="primary"
                  :icon="DocumentCopy"
                  :title="$t('CopyLabel')"
                  @click="copyText(row.topicPattern)"
                />
              </span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('ProductDataChannelEffectLabel')" width="120">
            <template #default="{ row }">
              <el-select
                :model-value="row.effect"
                size="small"
                :loading="savingDataChannelId === row.id"
                :disabled="savingDataChannelId === row.id"
                @change="(value) => handleDataChannelEffectChange(row, value)"
              >
                <el-option value="allow" :label="$t('AclEffectAllow')" />
                <el-option value="deny" :label="$t('AclEffectDeny')" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('ProductDataChannelEnabledLabel')" width="120">
            <template #default="{ row }">
              <el-switch
                :model-value="row.enabled"
                :loading="savingDataChannelId === row.id"
                :disabled="savingDataChannelId === row.id"
                @change="(value) => handleDataChannelEnabledChange(row, value)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="priority" :label="$t('ProductDataChannelPriorityLabel')" width="120" />
        </el-table>
      </div>

      <div class="detail-section">
        <div class="section-header">
          <div class="section-title">{{ $t('DevicePropertyRecordsTitle') }}</div>
          <el-button
            link
            type="primary"
            :loading="isRefreshingPropertyRecords"
            @click="refreshPropertyRecords"
          >
            {{ $t('RefreshButtonLabel') }}
          </el-button>
        </div>
        <el-table :data="propertyRecords" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="$t('DeviceRecordsEmpty')" />
          </template>
          <el-table-column prop="propertyIdentifier" :label="$t('DevicePropertyIdentifierLabel')" min-width="180" />
          <el-table-column prop="propertyValue" :label="$t('DevicePropertyValueLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="$t('DeviceRecordTimeLabel')" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt, row.deviceTimestamp) }}</template>
          </el-table-column>
          <el-table-column prop="topic" :label="$t('DeviceRecordTopicLabel')" min-width="240" show-overflow-tooltip />
          <el-table-column prop="traceId" :label="$t('DeviceRecordTraceIdLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="$t('OperationsLabel')" width="120" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPayloadDialog(row.payload)">{{ $t('DeviceRecordPayloadAction') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="propertyPageNum"
            v-model:page-size="propertyPageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="propertyTotal"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadPropertyRecords"
            @size-change="handlePropertyPageSizeChange"
          />
        </div>
      </div>

      <div class="detail-section">
        <div class="section-header">
          <div class="section-title">{{ $t('DeviceEventRecordsTitle') }}</div>
          <el-button
            link
            type="primary"
            :loading="isRefreshingEventRecords"
            @click="refreshEventRecords"
          >
            {{ $t('RefreshButtonLabel') }}
          </el-button>
        </div>
        <el-table :data="eventRecords" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="$t('DeviceRecordsEmpty')" />
          </template>
          <el-table-column prop="eventName" :label="$t('DeviceEventNameLabel')" min-width="180" />
          <el-table-column :label="$t('DeviceEventOutputLabel')" min-width="260">
            <template #default="{ row }">
              <div class="structured-cell">{{ formatStructuredLines(parseEventOutput(row.payload)) }}</div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('DeviceRecordTimeLabel')" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt, row.deviceTimestamp) }}</template>
          </el-table-column>
          <el-table-column prop="topic" :label="$t('DeviceRecordTopicLabel')" min-width="240" show-overflow-tooltip />
          <el-table-column prop="traceId" :label="$t('DeviceRecordTraceIdLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="$t('OperationsLabel')" width="120" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPayloadDialog(row.payload)">{{ $t('DeviceRecordPayloadAction') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="eventPageNum"
            v-model:page-size="eventPageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="eventTotal"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadEventRecords"
            @size-change="handleEventPageSizeChange"
          />
        </div>
      </div>

      <div class="detail-section">
        <div class="section-header">
          <div class="section-title">{{ $t('DeviceServiceRecordsTitle') }}</div>
          <el-button
            link
            type="primary"
            :loading="isRefreshingServiceRecords"
            @click="refreshServiceRecords"
          >
            {{ $t('RefreshButtonLabel') }}
          </el-button>
        </div>
        <el-table :data="serviceRecords" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="$t('DeviceRecordsEmpty')" />
          </template>
          <el-table-column prop="serviceName" :label="$t('DeviceServiceNameLabel')" min-width="180" />
          <el-table-column :label="$t('DeviceServiceDirectionLabel')" min-width="120">
            <template #default="{ row }">
              {{ row.direction === 'request' ? $t('DeviceServiceDirectionRequest') : $t('DeviceServiceDirectionReply') }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('DeviceServiceInputLabel')" min-width="260">
            <template #default="{ row }">
              <div class="structured-cell">{{ formatStructuredLines(parseServiceInput(row)) }}</div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('DeviceServiceOutputLabel')" min-width="260">
            <template #default="{ row }">
              <div class="structured-cell">{{ formatStructuredLines(parseServiceOutput(row)) }}</div>
            </template>
          </el-table-column>
          <el-table-column :label="$t('DeviceRecordTimeLabel')" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt, row.deviceTimestamp) }}</template>
          </el-table-column>
          <el-table-column prop="topic" :label="$t('DeviceRecordTopicLabel')" min-width="240" show-overflow-tooltip />
          <el-table-column prop="traceId" :label="$t('DeviceRecordTraceIdLabel')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="$t('OperationsLabel')" width="120" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPayloadDialog(row.payload)">{{ $t('DeviceRecordPayloadAction') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="servicePageNum"
            v-model:page-size="servicePageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="serviceTotal"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadServiceRecords"
            @size-change="handleServicePageSizeChange"
          />
        </div>
      </div>

      <div class="detail-section">
        <div class="section-header">
          <div class="section-title">{{ $t('DeviceConnectRecordsTitle') }}</div>
          <el-button
            link
            type="primary"
            :loading="isRefreshingConnectRecords"
            @click="refreshConnectRecords"
          >
            {{ $t('RefreshButtonLabel') }}
          </el-button>
        </div>
        <el-table :data="connectRecords" stripe border empty-text=" ">
          <template #empty>
            <el-empty :description="$t('DeviceRecordsEmpty')" />
          </template>
          <el-table-column :label="$t('DeviceConnectActionLabel')" min-width="120">
            <template #default="{ row }">
              <el-tag size="small" :type="row.action === 'connected' ? 'success' : 'info'">
                {{ row.action === 'connected' ? $t('Connected') : $t('Disconnected') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="clientId" :label="$t('DeviceConnectClientIdLabel')" min-width="160" show-overflow-tooltip />
          <el-table-column prop="ip" :label="$t('DeviceConnectIpLabel')" min-width="140" show-overflow-tooltip />
          <el-table-column prop="userAgent" :label="$t('DeviceConnectUserAgentLabel')" min-width="240" show-overflow-tooltip />
          <el-table-column :label="$t('DeviceRecordTimeLabel')" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="connectPageNum"
            v-model:page-size="connectPageSize"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="connectTotal"
            :page-sizes="[10, 20, 50, 100]"
            @current-change="loadConnectRecords"
            @size-change="handleConnectPageSizeChange"
          />
        </div>
      </div>

      <el-dialog
        :model-value="propertyChartVisible"
        :title="`${$t('DevicePropertyChartTitle')} - ${chartPropertyIdentifier || '-'}`"
        width="860px"
        align-center
        destroy-on-close
        @close="closePropertyChart"
      >
        <div v-if="chartPoints.length" class="property-chart">
          <div class="property-chart-meta">
            <span>{{ $t('DevicePropertyChartMinLabel') }}: {{ chartMin }}</span>
            <span>{{ $t('DevicePropertyChartMaxLabel') }}: {{ chartMax }}</span>
            <div class="property-chart-range">
              <span>{{ $t('DevicePropertyChartRangeLabel') }}</span>
              <el-radio-group v-model="chartLimit" size="small">
                <el-radio-button :label="10">10</el-radio-button>
                <el-radio-button :label="20">20</el-radio-button>
                <el-radio-button :label="50">50</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="property-chart-stage">
            <svg viewBox="0 0 760 280" class="property-chart-svg" preserveAspectRatio="none">
              <line x1="40" y1="20" x2="40" y2="240" class="chart-axis" />
              <line x1="40" y1="240" x2="720" y2="240" class="chart-axis" />
              <polyline :points="chartPolyline" class="chart-line" />
              <circle
                v-for="point in chartPoints"
                :key="point.key"
                :cx="point.x"
                :cy="point.y"
                r="4"
                class="chart-dot"
              />
              <text
                v-for="point in chartPoints"
                :key="`${point.key}-label`"
                :x="point.x"
                y="262"
                text-anchor="middle"
                class="chart-label"
              >
                {{ point.label }}
              </text>
              <circle
                v-for="point in chartPoints"
                :key="`${point.key}-hit`"
                :cx="point.x"
                :cy="point.y"
                r="12"
                class="chart-hit-area"
                @mouseenter="hoverPoint = point"
                @mouseleave="hoverPoint = null"
              />
            </svg>
            <div
              v-if="hoverPoint"
              class="chart-tooltip"
              :style="{ left: `${hoverPoint.x}px`, top: `${hoverPoint.y}px` }"
            >
              <div>{{ hoverPoint.fullLabel }}</div>
              <div>{{ $t('DevicePropertyValueLabel') }}: {{ hoverPoint.value }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else :description="$t('DevicePropertyChartEmpty')" />
      </el-dialog>

      <el-dialog
        :model-value="payloadDialogVisible"
        :title="$t('DeviceRecordPayloadTitle')"
        width="760px"
        align-center
        destroy-on-close
        @close="closePayloadDialog"
      >
        <pre class="payload-dialog-content">{{ payloadDialogContent || '-' }}</pre>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, DocumentCopy, Hide, RefreshRight, View } from '@element-plus/icons-vue'
import {
  getDevice,
  getDeviceCurrentProperties,
  getDeviceDataChannels,
  getDeviceConnectRecords,
  getDeviceEventRecords,
  getLatestDevicePropertyValue,
  getDevicePropertyRecords,
  getDeviceServiceRecords,
  resetDeviceSecret,
  updateDeviceDataChannel
} from '@/views/devices/DeviceApi'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const detail = ref({
  productKey: '',
  deviceKey: '',
  deviceName: '',
  address: '',
  deviceType: '',
  status: '',
  secret: '',
  protocolType: '',
  protocolVersion: ''
})
const deviceSecretVisible = ref(false)
const isResettingSecret = ref(false)
const displayDeviceSecret = computed(() => {
  const s = detail.value.secret
  if (!s) return '-'
  if (deviceSecretVisible.value) return s
  return '*'.repeat(s.length)
})
const propertyRecords = ref([])
const currentProperties = ref([])
const refreshingPropertyKey = ref('')
const isRefreshingPropertyRecords = ref(false)
const isRefreshingEventRecords = ref(false)
const isRefreshingServiceRecords = ref(false)
const isRefreshingConnectRecords = ref(false)
const dataChannels = ref([])
const savingDataChannelId = ref(null)
const eventRecords = ref([])
const serviceRecords = ref([])
const propertyTotal = ref(0)
const eventTotal = ref(0)
const serviceTotal = ref(0)
const connectRecords = ref([])
const connectTotal = ref(0)
const propertyPageNum = ref(1)
const eventPageNum = ref(1)
const servicePageNum = ref(1)
const connectPageNum = ref(1)
const propertyPageSize = ref(10)
const eventPageSize = ref(10)
const servicePageSize = ref(10)
const connectPageSize = ref(10)
const propertyChartVisible = ref(false)
const chartPropertyIdentifier = ref('')
const chartLimit = ref(20)
const hoverPoint = ref(null)
const payloadDialogVisible = ref(false)
const payloadDialogContent = ref('')
const currentPropertyCards = computed(() => {
  return (currentProperties.value || []).map((item) => ({
    ...item,
    isNumeric: isNumericValue(item?.propertyValue)
  }))
})

const isPropertyRefreshing = (propertyIdentifier) => refreshingPropertyKey.value === propertyIdentifier
const chartRecords = computed(() => propertyRecords.value
  .filter((item) => item.propertyIdentifier === chartPropertyIdentifier.value)
  .slice()
  .slice(-chartLimit.value)
  .reverse())
const chartValues = computed(() => chartRecords.value
  .map((item) => Number(item.propertyValue))
  .filter((value) => !Number.isNaN(value)))
const chartMin = computed(() => chartValues.value.length ? Math.min(...chartValues.value) : 0)
const chartMax = computed(() => chartValues.value.length ? Math.max(...chartValues.value) : 0)
const chartPoints = computed(() => {
  const records = chartRecords.value
  if (!records.length) return []
  const values = records.map((item) => Number(item.propertyValue))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const width = 680
  const height = 220
  return records.map((item, index) => {
    const value = Number(item.propertyValue)
    const ratioX = records.length === 1 ? 0.5 : index / (records.length - 1)
    const ratioY = max === min ? 0.5 : (value - min) / (max - min)
    return {
      key: `${item.propertyIdentifier}-${item.createdAt}-${index}`,
      x: 40 + (width * ratioX),
      y: 240 - (height * ratioY),
      label: formatChartLabel(item.createdAt, item.deviceTimestamp),
      fullLabel: formatTime(item.createdAt, item.deviceTimestamp),
      value
    }
  })
})
const chartPolyline = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))

const loadDetail = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  try {
    const device = await getDevice(productKey, deviceKey)
    deviceSecretVisible.value = false
    detail.value = {
      productKey: device.productKey || '',
      deviceKey: device.deviceKey || '',
      deviceName: device.deviceName || '',
      address: device.address != null ? String(device.address).trim() : '',
      deviceType: device.deviceType || '',
      status: device.status || '',
      secret: device.secret || '',
      protocolType: device.protocolType || '',
      protocolVersion: device.protocolVersion || ''
    }
    await Promise.all([
      loadCurrentProperties(),
      loadDataChannels(),
      loadPropertyRecords(),
      loadEventRecords(),
      loadServiceRecords(),
      loadConnectRecords()
    ])
  } catch (error) {
    ElMessage.error(error.message || 'Failed to load device detail')
  }
}

const copyText = async (text) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('CopyLabel'))
  } catch {
    ElMessage.error(t('RequestFailedNotice'))
  }
}

const resetSecret = async () => {
  if (isResettingSecret.value) return
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  try {
    await ElMessageBox.confirm(
      t('ResetSecretConfirmLabel'),
      t('ResetSecretButtonText'),
      {
        confirmButtonText: t('ConfirmButtonText'),
        cancelButtonText: t('CancelButtonText'),
        type: 'warning'
      }
    )
    isResettingSecret.value = true
    const updated = await resetDeviceSecret(productKey, deviceKey)
    if (updated?.secret) {
      detail.value.secret = updated.secret
    }
    deviceSecretVisible.value = false
    ElMessage.success(t('ResetSecretSuccess'))
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    isResettingSecret.value = false
  }
}

const loadPropertyRecords = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  const data = await getDevicePropertyRecords(productKey, deviceKey, {
    pageNum: propertyPageNum.value,
    pageSize: propertyPageSize.value
  })
  propertyRecords.value = data.records || []
  propertyTotal.value = data.total || 0
  propertyPageNum.value = data.pageNum || propertyPageNum.value
  propertyPageSize.value = data.pageSize || propertyPageSize.value
}

const loadCurrentProperties = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  const rows = await getDeviceCurrentProperties(productKey, deviceKey)
  currentProperties.value = (rows || []).map((item) => ({
    propertyIdentifier: item.propertyIdentifier || '',
    propertyValue: item.propertyValue ?? '',
    deviceTimestamp: item.deviceTimestamp ?? null,
    createdAt: item.createdAt || null
  }))
}

const refreshPropertyRecords = async () => {
  if (isRefreshingPropertyRecords.value) return
  const prevPageNum = propertyPageNum.value
  try {
    isRefreshingPropertyRecords.value = true
    propertyPageNum.value = 1
    await loadPropertyRecords()
  } catch (error) {
    propertyPageNum.value = prevPageNum
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    isRefreshingPropertyRecords.value = false
  }
}

const mergeLatestPropertyRecord = (latest) => {
  if (!latest?.propertyIdentifier) return
  const records = [...currentProperties.value]
  const firstMatchIndex = records.findIndex((item) => item?.propertyIdentifier === latest.propertyIdentifier)
  if (firstMatchIndex >= 0) {
    // Replace in place so card order stays stable.
    records[firstMatchIndex] = {
      ...records[firstMatchIndex],
      propertyValue: latest.propertyValue ?? '',
      deviceTimestamp: latest.deviceTimestamp ?? null,
      createdAt: latest.createdAt || null
    }
  } else {
    records.push({
      propertyIdentifier: latest.propertyIdentifier,
      propertyValue: latest.propertyValue ?? '',
      deviceTimestamp: latest.deviceTimestamp ?? null,
      createdAt: latest.createdAt || null
    })
  }
  currentProperties.value = records
}

const refreshCurrentPropertyValue = async (propertyIdentifier) => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey || !propertyIdentifier || refreshingPropertyKey.value) return
  try {
    refreshingPropertyKey.value = propertyIdentifier
    const latest = await getLatestDevicePropertyValue(productKey, deviceKey, propertyIdentifier)
    mergeLatestPropertyRecord(latest)
  } catch (error) {
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    refreshingPropertyKey.value = ''
  }
}

const loadDataChannels = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  const channels = await getDeviceDataChannels(productKey, deviceKey)
  dataChannels.value = (channels || []).map((item) => ({
    id: item.id,
    action: item.action || '',
    topicPattern: item.topicPattern || '',
    effect: (item.effect || 'allow').toLowerCase(),
    enabled: Boolean(item.enabled),
    priority: item.priority ?? 0
  }))
}

const saveDeviceDataChannel = async (row, payload) => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey || !row?.id) return
  try {
    savingDataChannelId.value = row.id
    await updateDeviceDataChannel(productKey, deviceKey, row.id, {
      effect: payload.effect,
      enabled: payload.enabled
    })
    ElMessage.success(t('ProductDataChannelUpdateSuccess'))
  } finally {
    savingDataChannelId.value = null
  }
}

const handleDataChannelEffectChange = async (row, value) => {
  const prev = row.effect
  row.effect = value
  try {
    await saveDeviceDataChannel(row, {
      effect: row.effect,
      enabled: row.enabled
    })
  } catch (error) {
    row.effect = prev
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

const handleDataChannelEnabledChange = async (row, value) => {
  const prev = row.enabled
  row.enabled = Boolean(value)
  try {
    await saveDeviceDataChannel(row, {
      effect: row.effect,
      enabled: row.enabled
    })
  } catch (error) {
    row.enabled = prev
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

const loadEventRecords = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  const data = await getDeviceEventRecords(productKey, deviceKey, {
    pageNum: eventPageNum.value,
    pageSize: eventPageSize.value
  })
  eventRecords.value = data.records || []
  eventTotal.value = data.total || 0
  eventPageNum.value = data.pageNum || eventPageNum.value
  eventPageSize.value = data.pageSize || eventPageSize.value
}

const refreshEventRecords = async () => {
  if (isRefreshingEventRecords.value) return
  const prevPageNum = eventPageNum.value
  try {
    isRefreshingEventRecords.value = true
    eventPageNum.value = 1
    await loadEventRecords()
  } catch (error) {
    eventPageNum.value = prevPageNum
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    isRefreshingEventRecords.value = false
  }
}

const loadServiceRecords = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  const data = await getDeviceServiceRecords(productKey, deviceKey, {
    pageNum: servicePageNum.value,
    pageSize: servicePageSize.value
  })
  serviceRecords.value = data.records || []
  serviceTotal.value = data.total || 0
  servicePageNum.value = data.pageNum || servicePageNum.value
  servicePageSize.value = data.pageSize || servicePageSize.value
}

const refreshServiceRecords = async () => {
  if (isRefreshingServiceRecords.value) return
  const prevPageNum = servicePageNum.value
  try {
    isRefreshingServiceRecords.value = true
    servicePageNum.value = 1
    await loadServiceRecords()
  } catch (error) {
    servicePageNum.value = prevPageNum
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    isRefreshingServiceRecords.value = false
  }
}

const refreshConnectRecords = async () => {
  if (isRefreshingConnectRecords.value) return
  const prevPageNum = connectPageNum.value
  try {
    isRefreshingConnectRecords.value = true
    connectPageNum.value = 1
    await loadConnectRecords()
  } catch (error) {
    connectPageNum.value = prevPageNum
    ElMessage.error(error?.message || t('RequestFailedNotice'))
  } finally {
    isRefreshingConnectRecords.value = false
  }
}

const loadConnectRecords = async () => {
  const productKey = route.params.productKey
  const deviceKey = route.params.deviceKey
  if (!productKey || !deviceKey) return
  const data = await getDeviceConnectRecords(productKey, deviceKey, {
    pageNum: connectPageNum.value,
    pageSize: connectPageSize.value
  })
  connectRecords.value = data.records || []
  connectTotal.value = data.total || 0
  connectPageNum.value = data.pageNum || connectPageNum.value
  connectPageSize.value = data.pageSize || connectPageSize.value
}

const formatTime = (createdAt, deviceTimestamp) => {
  if (deviceTimestamp) {
    const value = new Date(Number(deviceTimestamp))
    if (!Number.isNaN(value.getTime())) {
      return value.toLocaleString()
    }
  }
  if (!createdAt) return '-'
  const value = new Date(createdAt)
  return Number.isNaN(value.getTime()) ? createdAt : value.toLocaleString()
}

const formatChartLabel = (createdAt, deviceTimestamp) => {
  const raw = deviceTimestamp ? new Date(Number(deviceTimestamp)) : new Date(createdAt)
  if (Number.isNaN(raw.getTime())) return '-'
  const hours = `${raw.getHours()}`.padStart(2, '0')
  const minutes = `${raw.getMinutes()}`.padStart(2, '0')
  const seconds = `${raw.getSeconds()}`.padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

const tryParseJson = (value) => {
  if (!value) return null
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

const formatStructuredValue = (value) => {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'string') return value
  try {
    return JSON.stringify(value)
  } catch {
    return String(value)
  }
}

const formatStructuredLines = (value) => {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'string') return value
  if (Array.isArray(value)) {
    return value.map((item) => formatStructuredLines(item)).join('\n')
  }
  if (typeof value === 'object') {
    return Object.entries(value).map(([key, val]) => `${key}: ${formatStructuredValue(val)}`).join('\n')
  }
  return String(value)
}

const parseEventOutput = (payload) => {
  const parsed = tryParseJson(payload)
  return parsed ?? payload
}

const parseServiceInput = (row) => {
  if (row.direction !== 'request') return '-'
  const parsed = tryParseJson(row.payload)
  if (parsed?.params !== undefined) return parsed.params
  return parsed ?? row.payload
}

const parseServiceOutput = (row) => {
  const rawOutput = row.outputPayload || (row.direction === 'reply' ? row.payload : null)
  if (!rawOutput) return '-'
  const parsed = tryParseJson(rawOutput)
  if (parsed?.data !== undefined) return parsed.data
  return parsed ?? rawOutput
}

const isNumericValue = (value) => {
  if (value === null || value === undefined || value === '') return false
  return !Number.isNaN(Number(value))
}

const openPropertyChart = (identifier) => {
  chartPropertyIdentifier.value = identifier
  chartLimit.value = 20
  hoverPoint.value = null
  propertyChartVisible.value = true
}

const closePropertyChart = () => {
  propertyChartVisible.value = false
  chartPropertyIdentifier.value = ''
  hoverPoint.value = null
}

const openPayloadDialog = (payload) => {
  payloadDialogContent.value = payload || ''
  payloadDialogVisible.value = true
}

const closePayloadDialog = () => {
  payloadDialogVisible.value = false
  payloadDialogContent.value = ''
}

const handlePropertyPageSizeChange = (value) => {
  propertyPageSize.value = value
  propertyPageNum.value = 1
  loadPropertyRecords()
}

const handleEventPageSizeChange = (value) => {
  eventPageSize.value = value
  eventPageNum.value = 1
  loadEventRecords()
}

const handleServicePageSizeChange = (value) => {
  servicePageSize.value = value
  servicePageNum.value = 1
  loadServiceRecords()
}

const handleConnectPageSizeChange = (value) => {
  connectPageSize.value = value
  connectPageNum.value = 1
  loadConnectRecords()
}

onMounted(loadDetail)

watch(() => [route.params.productKey, route.params.deviceKey], () => {
  propertyPageNum.value = 1
  eventPageNum.value = 1
  servicePageNum.value = 1
  connectPageNum.value = 1
  loadDetail()
})
</script>

<style scoped>
.page-toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-button {
  padding: 8px;
  font-size: 18px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.detail-section + .detail-section {
  margin-top: 24px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.section-header {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.secret-row {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.secret-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 13px;
  line-height: 28px;
}

.secret-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  vertical-align: middle;
}

.secret-actions :deep(.secret-icon-btn.el-button.is-link),
.secret-row :deep(.secret-icon-btn.el-button.is-link) {
  margin: 0;
  padding: 0;
  width: 28px;
  height: 28px;
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.secret-actions :deep(.secret-icon-btn .el-icon),
.secret-row :deep(.secret-icon-btn .el-icon) {
  font-size: 16px;
}

.property-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.property-card-label {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-secondary);
}

.property-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.property-card-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.property-chart-button {
  padding: 0;
  font-size: 12px;
}

.property-refresh-button {
  padding: 0;
  font-size: 12px;
}

.property-card-value {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--el-text-color-primary);
  word-break: break-word;
}

.property-card-time {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.property-chart {
  width: 100%;
}

.property-chart-meta {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.property-chart-range {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.property-chart-stage {
  position: relative;
}

.property-chart-svg {
  width: 100%;
  height: 280px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.chart-axis {
  stroke: var(--el-border-color);
  stroke-width: 1;
}

.chart-line {
  fill: none;
  stroke: var(--el-color-primary);
  stroke-width: 2.5;
}

.chart-dot {
  fill: var(--el-color-primary);
}

.chart-hit-area {
  fill: transparent;
  cursor: pointer;
}

.chart-label {
  fill: var(--el-text-color-secondary);
  font-size: 11px;
}

.chart-tooltip {
  position: absolute;
  transform: translate(-50%, calc(-100% - 12px));
  min-width: 140px;
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(31, 41, 55, 0.92);
  color: #fff;
  font-size: 12px;
  line-height: 1.5;
  pointer-events: none;
  white-space: nowrap;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.18);
}

.payload-dialog-content {
  margin: 0;
  padding: 16px;
  max-height: 420px;
  overflow: auto;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
  word-break: break-word;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.6;
}

.structured-cell {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

.topic-pattern-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
}

.topic-pattern-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>


<template>
  <el-card shadow="always">
    <el-tabs v-model="activeKind" class="notify-channel-tabs" @tab-change="onTabChange">
      <el-tab-pane :label="t('NotifyChannelTabEmail')" name="EMAIL" />
      <el-tab-pane :label="t('NotifyChannelTabSms')" name="SMS" />
    </el-tabs>

    <div class="toolbar">
      <el-button v-permission="'iot.notify.edit'" type="primary" :icon="Plus" @click="openCreate">
        {{ t('NewButtonLabel') }}
      </el-button>
      <el-button :icon="Refresh" @click="load">{{ t('RefreshButtonLabel') }}</el-button>
    </div>

    <el-table :data="rows" stripe border show-overflow-tooltip empty-text=" ">
      <template #empty>
        <el-empty :description="t('NotifyChannelEmpty')" />
      </template>
      <el-table-column prop="name" :label="t('NotifyChannelName')" min-width="160" show-overflow-tooltip />
      <el-table-column :label="t('NotifyChannelColumnProvider')" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ providerLabel(row.provider) }}</template>
      </el-table-column>
      <el-table-column :label="t('StatusLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? t('Enabled') : t('Disabled') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" :label="t('IntegrationUpdatedAt')" min-width="160" />
      <el-table-column :label="t('OperationsLabel')" width="380" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">{{ t('DetailLabel') }}</el-button>
          <el-button v-permission="'iot.notify.edit'" link type="primary" @click="openEdit(row)">{{ t('EditLabel') }}</el-button>
          <el-button v-permission="'iot.notify.edit'" link type="primary" @click="toggleEnabled(row)">
            {{ row.enabled ? t('Disable') : t('Enable') }}
          </el-button>
          <el-button v-permission="'iot.notify.edit'" link type="primary" @click="openTest(row)">{{ t('NotifySettingsTest') }}</el-button>
          <el-button v-permission="'iot.notify.edit'" link type="primary" @click="confirmDelete(row)">{{ t('DeleteLabel') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        background
        layout="total, sizes, prev, pager, next"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        @current-change="load"
        @size-change="handlePageSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close @closed="resetDialog">
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <el-form-item v-if="dialogMode === 'create'" prop="provider" :label="t('NotifyChannelColumnProvider')" :required="true">
          <el-select v-model="form.provider" style="width: 100%" @change="onProviderChange">
            <el-option v-if="activeKind === 'EMAIL'" :label="t('NotifyChannelProviderSmtpLabel')" value="SMTP" />
            <el-option v-if="activeKind === 'SMS'" :label="t('NotifyChannelProviderAliyunLabel')" value="ALIYUN" />
            <el-option v-if="activeKind === 'SMS'" :label="t('NotifyChannelProviderTwilio')" value="TWILIO" />
            <el-option v-if="activeKind === 'SMS'" :label="t('NotifyChannelProviderAwsSns')" value="AWS_SNS" />
          </el-select>
        </el-form-item>
        <el-form-item prop="name" :label="t('NotifyChannelName')" :required="true">
          <el-input v-model="form.name" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <template v-if="activeKind === 'EMAIL'">
          <el-form-item prop="smtpHost" :label="t('NotifySettingsSmtpHost')" :required="true">
            <el-input v-model="form.smtpHost" />
          </el-form-item>
          <el-form-item prop="smtpPort" :label="t('NotifySettingsSmtpPort')" :required="true">
            <el-input-number v-model="form.smtpPort" :min="1" :max="65535" style="width: 100%" />
          </el-form-item>
          <el-form-item :label="t('NotifySettingsSmtpSsl')">
            <el-switch v-model="form.smtpSsl" />
          </el-form-item>
          <el-form-item prop="smtpUsername" :label="t('NotifySettingsSmtpUser')" :required="true">
            <el-input v-model="form.smtpUsername" />
          </el-form-item>
          <el-form-item
            prop="smtpPassword"
            :label="t('NotifySettingsSmtpPassword')"
            :required="dialogMode === 'create'"
          >
            <el-input v-model="form.smtpPassword" type="password" show-password />
            <div class="hint">{{ t('NotifySettingsSmtpPasswordHint') }}</div>
          </el-form-item>
          <el-form-item prop="mailFrom" :label="t('NotifySettingsMailFrom')" :required="true">
            <el-input v-model="form.mailFrom" />
          </el-form-item>
          <el-form-item :label="t('NotifySettingsMailFromDisplay')">
            <el-input v-model="form.mailFromDisplay" />
          </el-form-item>
        </template>
        <template v-else-if="activeKind === 'SMS' && form.provider === 'ALIYUN'">
          <el-form-item prop="smsAccessKeyId" :label="t('NotifySettingsSmsAk')" :required="true">
            <el-input v-model="form.smsAccessKeyId" />
          </el-form-item>
          <el-form-item
            prop="smsAccessKeySecret"
            :label="t('NotifySettingsSmsSk')"
            :required="dialogMode === 'create'"
          >
            <el-input v-model="form.smsAccessKeySecret" type="password" show-password />
            <div class="hint">{{ t('NotifySettingsSmsSkHint') }}</div>
          </el-form-item>
          <el-form-item prop="smsSignName" :label="t('NotifySettingsSmsSign')" :required="true">
            <el-input v-model="form.smsSignName" />
          </el-form-item>
          <el-form-item prop="smsTemplateCode" :label="t('NotifySettingsSmsTpl')" :required="true">
            <el-input v-model="form.smsTemplateCode" />
          </el-form-item>
          <el-form-item :label="t('NotifySettingsSmsRegion')">
            <el-input v-model="form.smsRegionId" />
          </el-form-item>
        </template>
        <template v-else-if="activeKind === 'SMS' && form.provider === 'TWILIO'">
          <p class="hint">{{ t('NotifyChannelTwilioHint') }}</p>
          <el-form-item prop="twilioAccountSid" :label="t('NotifyChannelTwilioAccountSid')" :required="true">
            <el-input v-model="form.twilioAccountSid" />
          </el-form-item>
          <el-form-item
            prop="twilioAuthToken"
            :label="t('NotifyChannelTwilioAuthToken')"
            :required="dialogMode === 'create'"
          >
            <el-input v-model="form.twilioAuthToken" type="password" show-password />
          </el-form-item>
          <el-form-item prop="twilioFrom" :label="t('NotifyChannelTwilioFrom')">
            <el-input v-model="form.twilioFrom" :placeholder="t('NotifyChannelTwilioFromPlaceholder')" />
          </el-form-item>
          <el-form-item prop="twilioMessagingServiceSid" :label="t('NotifyChannelTwilioMessagingServiceSid')">
            <el-input v-model="form.twilioMessagingServiceSid" :placeholder="t('NotifyChannelTwilioMsidPlaceholder')" />
          </el-form-item>
        </template>
        <template v-else-if="activeKind === 'SMS' && form.provider === 'AWS_SNS'">
          <p class="hint">{{ t('NotifyChannelAwsSnsHint') }}</p>
          <el-form-item prop="awsAccessKeyId" :label="t('NotifyChannelAwsAccessKeyId')" :required="true">
            <el-input v-model="form.awsAccessKeyId" />
          </el-form-item>
          <el-form-item
            prop="awsSecretAccessKey"
            :label="t('NotifyChannelAwsSecretAccessKey')"
            :required="dialogMode === 'create'"
          >
            <el-input v-model="form.awsSecretAccessKey" type="password" show-password />
          </el-form-item>
          <el-form-item prop="awsRegion" :label="t('NotifyChannelAwsRegion')" :required="true">
            <el-input v-model="form.awsRegion" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitDialog">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" :title="t('DetailLabel')" width="560px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="t('NotifyChannelName')">
          <el-input :model-value="detail.name" readonly />
        </el-form-item>
        <el-form-item :label="t('AlarmNotifyChannel')">
          <el-input :model-value="channelKindLabel(detail.channelKind)" readonly />
        </el-form-item>
        <el-form-item :label="t('NotifyChannelColumnProvider')">
          <el-input :model-value="providerLabel(detail.provider)" readonly />
        </el-form-item>
        <el-form-item :label="t('StatusLabel')">
          <el-input :model-value="detail.enabled ? t('Enabled') : t('Disabled')" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationUpdatedAt')">
          <el-input :model-value="detail.updatedAt || '—'" readonly />
        </el-form-item>
        <el-form-item :label="t('IntegrationConfigJson')">
          <el-input :model-value="detail.configJson" type="textarea" :rows="8" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailDialog.visible = false">{{ t('CancelButtonText') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testVisible" :title="t('NotifySettingsTestTitle')" width="480px" destroy-on-close @closed="resetTest">
      <p class="hint">{{ t('NotifyChannelTestHint') }}</p>
      <el-form label-position="top">
        <el-form-item v-if="testRow && testRow.channelKind === 'EMAIL'" :label="t('NotifySettingsTestEmailPh')">
          <el-input v-model="testEmail" />
        </el-form-item>
        <el-form-item v-if="testRow && testRow.channelKind === 'SMS'" :label="t('NotifySettingsTestPhonePh')">
          <el-input v-model="testPhone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testVisible = false">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" :loading="testing" @click="runTest">{{ t('ConfirmButtonText') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  createNotifyChannel,
  deleteNotifyChannel,
  getNotifyChannel,
  listNotifyChannels,
  testNotifyChannel,
  updateNotifyChannel
} from '@/views/notify/NotifyApi'
import { parsePage } from '@/views/alarm/alarmRuleSupport'

const { t } = useI18n()
const activeKind = ref('EMAIL')
const rows = ref([])
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogMode = ref('create')
const saving = ref(false)
const editId = ref(null)
const formRef = ref(null)
const form = reactive({
  provider: 'SMTP',
  name: '',
  enabled: true,
  smtpHost: '',
  smtpPort: 465,
  smtpSsl: true,
  smtpUsername: '',
  smtpPassword: '',
  mailFrom: '',
  mailFromDisplay: '',
  smsAccessKeyId: '',
  smsAccessKeySecret: '',
  smsSignName: '',
  smsTemplateCode: '',
  smsRegionId: 'cn-hangzhou',
  twilioAccountSid: '',
  twilioAuthToken: '',
  twilioFrom: '',
  twilioMessagingServiceSid: '',
  awsAccessKeyId: '',
  awsSecretAccessKey: '',
  awsRegion: 'us-east-1'
})

const formRules = computed(() => {
  const rules = {
    name: [{ required: true, message: t('NameRequired'), trigger: ['blur', 'change'] }]
  }
  if (dialogMode.value === 'create') {
    rules.provider = [{ required: true, message: t('NotifyChannelProviderRequired'), trigger: 'change' }]
  }
  if (activeKind.value === 'EMAIL') {
    rules.smtpHost = [{ required: true, message: t('NotifyChannelSmtpHostRequired'), trigger: ['blur', 'change'] }]
    rules.smtpPort = [
      {
        validator: (_, v, cb) => {
          if (v == null || v === '') {
            cb(new Error(t('NotifyChannelSmtpPortRequired')))
            return
          }
          const n = Number(v)
          if (!Number.isFinite(n) || n < 1 || n > 65535) {
            cb(new Error(t('NotifyChannelSmtpPortInvalid')))
            return
          }
          cb()
        },
        trigger: ['blur', 'change']
      }
    ]
    rules.smtpUsername = [{ required: true, message: t('NotifyChannelSmtpUserRequired'), trigger: ['blur', 'change'] }]
    rules.mailFrom = [{ required: true, message: t('NotifyChannelMailFromRequired'), trigger: ['blur', 'change'] }]
    if (dialogMode.value === 'create') {
      rules.smtpPassword = [{ required: true, message: t('NotifyChannelSmtpPasswordRequired'), trigger: ['blur', 'change'] }]
    }
  }
  const pv = String(form.provider || '').toUpperCase()
  if (activeKind.value === 'SMS' && pv === 'ALIYUN') {
    rules.smsAccessKeyId = [{ required: true, message: t('NotifyChannelSmsAkRequired'), trigger: ['blur', 'change'] }]
    if (dialogMode.value === 'create') {
      rules.smsAccessKeySecret = [
        { required: true, message: t('NotifyChannelAliyunSecretRequired'), trigger: ['blur', 'change'] }
      ]
    }
    rules.smsSignName = [{ required: true, message: t('NotifyChannelSmsSignRequired'), trigger: ['blur', 'change'] }]
    rules.smsTemplateCode = [{ required: true, message: t('NotifyChannelSmsTplRequired'), trigger: ['blur', 'change'] }]
  }
  if (activeKind.value === 'SMS' && pv === 'TWILIO') {
    rules.twilioAccountSid = [
      { required: true, message: t('NotifyChannelTwilioAccountSidRequired'), trigger: ['blur', 'change'] }
    ]
    if (dialogMode.value === 'create') {
      rules.twilioAuthToken = [
        { required: true, message: t('NotifyChannelTwilioAuthTokenRequired'), trigger: ['blur', 'change'] }
      ]
    }
    rules.twilioFrom = [
      {
        validator: (_, v, cb) => {
          const fromOk = v && String(v).trim()
          const msidOk = form.twilioMessagingServiceSid && String(form.twilioMessagingServiceSid).trim()
          if (!fromOk && !msidOk) {
            cb(new Error(t('NotifyChannelTwilioFromOrMsidRequired')))
            return
          }
          cb()
        },
        trigger: ['blur', 'change']
      }
    ]
    rules.twilioMessagingServiceSid = [
      {
        validator: (_, v, cb) => {
          const fromOk = form.twilioFrom && String(form.twilioFrom).trim()
          const msidOk = v && String(v).trim()
          if (!fromOk && !msidOk) {
            cb(new Error(t('NotifyChannelTwilioFromOrMsidRequired')))
            return
          }
          cb()
        },
        trigger: ['blur', 'change']
      }
    ]
  }
  if (activeKind.value === 'SMS' && pv === 'AWS_SNS') {
    rules.awsAccessKeyId = [
      { required: true, message: t('NotifyChannelAwsAccessKeyIdRequired'), trigger: ['blur', 'change'] }
    ]
    if (dialogMode.value === 'create') {
      rules.awsSecretAccessKey = [
        { required: true, message: t('NotifyChannelAwsSecretAccessKeyRequired'), trigger: ['blur', 'change'] }
      ]
    }
    rules.awsRegion = [{ required: true, message: t('NotifyChannelAwsRegionRequired'), trigger: ['blur', 'change'] }]
  }
  return rules
})

const detailDialog = reactive({ visible: false })
const detail = reactive({
  name: '',
  channelKind: '',
  provider: '',
  enabled: false,
  updatedAt: '',
  configJson: ''
})

const testVisible = ref(false)
const testing = ref(false)
const testRow = ref(null)
const testEmail = ref('')
const testPhone = ref('')

const dialogTitle = computed(() => (dialogMode.value === 'create' ? t('CreateTitle') : t('EditTitle')))

function providerLabel(p) {
  const u = String(p || '').toUpperCase()
  if (u === 'SMTP') return t('NotifyChannelProviderSmtpLabel')
  if (u === 'ALIYUN') return t('NotifyChannelProviderAliyunLabel')
  if (u === 'TWILIO') return t('NotifyChannelProviderTwilio')
  if (u === 'AWS_SNS') return t('NotifyChannelProviderAwsSns')
  return p || '—'
}

function channelKindLabel(kind) {
  const u = String(kind || '').toUpperCase()
  if (u === 'EMAIL') return t('AlarmNotifyChannelEmail')
  if (u === 'SMS') return t('AlarmNotifyChannelSms')
  return kind || '—'
}

function formatConfigJson(raw) {
  if (raw == null) return ''
  if (typeof raw === 'string') return raw
  try {
    return JSON.stringify(raw, null, 2)
  } catch {
    return String(raw)
  }
}

function onTabChange() {
  page.pageNum = 1
  load()
}

function handlePageSizeChange() {
  page.pageNum = 1
  load()
}

function resetChannelConfigFields() {
  form.smtpHost = ''
  form.smtpPort = 465
  form.smtpSsl = true
  form.smtpUsername = ''
  form.smtpPassword = ''
  form.mailFrom = ''
  form.mailFromDisplay = ''
  form.smsAccessKeyId = ''
  form.smsAccessKeySecret = ''
  form.smsSignName = ''
  form.smsTemplateCode = ''
  form.smsRegionId = 'cn-hangzhou'
  form.twilioAccountSid = ''
  form.twilioAuthToken = ''
  form.twilioFrom = ''
  form.twilioMessagingServiceSid = ''
  form.awsAccessKeyId = ''
  form.awsSecretAccessKey = ''
  form.awsRegion = 'us-east-1'
}

function resetFormDefaults() {
  form.provider = activeKind.value === 'EMAIL' ? 'SMTP' : 'ALIYUN'
  form.name = ''
  form.enabled = true
  resetChannelConfigFields()
}

function onProviderChange() {
  /* reserved */
}

function openCreate() {
  dialogMode.value = 'create'
  editId.value = null
  resetFormDefaults()
  dialogVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate?.()
  })
}

function openDetail(row) {
  detail.name = row.name || ''
  detail.channelKind = row.channelKind || ''
  detail.provider = row.provider || ''
  detail.enabled = !!row.enabled
  detail.updatedAt = row.updatedAt || ''
  detail.configJson = formatConfigJson(row.configJson)
  detailDialog.visible = true
}

function parseConfigToForm(raw) {
  resetChannelConfigFields()
  let o = {}
  try {
    o = typeof raw === 'string' && raw ? JSON.parse(raw) : raw && typeof raw === 'object' ? raw : {}
  } catch {
    o = {}
  }
  if (activeKind.value === 'EMAIL') {
    form.smtpHost = o.smtpHost || ''
    form.smtpPort = o.smtpPort != null ? Number(o.smtpPort) : 465
    form.smtpSsl = o.smtpSsl !== false
    form.smtpUsername = o.smtpUsername || ''
    form.smtpPassword = ''
    form.mailFrom = o.mailFrom || ''
    form.mailFromDisplay = o.mailFromDisplay || ''
  } else if (form.provider === 'ALIYUN') {
    form.smsAccessKeyId = o.smsAccessKeyId || ''
    form.smsAccessKeySecret = ''
    form.smsSignName = o.smsSignName || ''
    form.smsTemplateCode = o.smsTemplateCode || ''
    form.smsRegionId = o.smsRegionId || 'cn-hangzhou'
  } else if (form.provider === 'TWILIO') {
    form.twilioAccountSid = o.twilioAccountSid || ''
    form.twilioAuthToken = ''
    form.twilioFrom = o.twilioFrom || ''
    form.twilioMessagingServiceSid = o.twilioMessagingServiceSid || ''
  } else if (form.provider === 'AWS_SNS') {
    form.awsAccessKeyId = o.awsAccessKeyId || ''
    form.awsSecretAccessKey = ''
    form.awsRegion = o.awsRegion || 'us-east-1'
  }
}

async function openEdit(row) {
  dialogMode.value = 'edit'
  editId.value = row.id
  try {
    const d = await getNotifyChannel(row.id)
    form.provider = String(d.provider || row.provider || '').toUpperCase() || (activeKind.value === 'EMAIL' ? 'SMTP' : 'ALIYUN')
    form.name = d.name != null && String(d.name).trim() !== '' ? d.name : row.name || ''
    form.enabled = d.enabled !== undefined && d.enabled !== null ? !!d.enabled : !!row.enabled
    parseConfigToForm(d.configJson)
    dialogVisible.value = true
    nextTick(() => {
      formRef.value?.clearValidate?.()
    })
  } catch {
    /* http interceptor */
  }
}

function resetDialog() {
  editId.value = null
  formRef.value?.resetFields?.()
}

function buildConfigJson() {
  if (activeKind.value === 'EMAIL') {
    return JSON.stringify({
      smtpHost: form.smtpHost,
      smtpPort: form.smtpPort,
      smtpSsl: !!form.smtpSsl,
      smtpUsername: form.smtpUsername,
      smtpPassword: form.smtpPassword,
      mailFrom: form.mailFrom,
      mailFromDisplay: form.mailFromDisplay
    })
  }
  if (form.provider === 'ALIYUN') {
    const o = {
      smsAccessKeyId: form.smsAccessKeyId,
      smsSignName: form.smsSignName,
      smsTemplateCode: form.smsTemplateCode,
      smsRegionId: form.smsRegionId || 'cn-hangzhou'
    }
    if (form.smsAccessKeySecret && String(form.smsAccessKeySecret).trim()) {
      o.smsAccessKeySecret = String(form.smsAccessKeySecret).trim()
    }
    return JSON.stringify(o)
  }
  if (form.provider === 'TWILIO') {
    const o = {
      twilioAccountSid: String(form.twilioAccountSid || '').trim(),
      twilioFrom: String(form.twilioFrom || '').trim(),
      twilioMessagingServiceSid: String(form.twilioMessagingServiceSid || '').trim()
    }
    if (form.twilioAuthToken && String(form.twilioAuthToken).trim()) {
      o.twilioAuthToken = String(form.twilioAuthToken).trim()
    }
    return JSON.stringify(o)
  }
  if (form.provider === 'AWS_SNS') {
    const o = {
      awsAccessKeyId: String(form.awsAccessKeyId || '').trim(),
      awsRegion: String(form.awsRegion || 'us-east-1').trim()
    }
    if (form.awsSecretAccessKey && String(form.awsSecretAccessKey).trim()) {
      o.awsSecretAccessKey = String(form.awsSecretAccessKey).trim()
    }
    return JSON.stringify(o)
  }
  return '{}'
}

async function submitDialog() {
  try {
    await formRef.value?.validate?.()
  } catch {
    return
  }
  saving.value = true
  try {
    const configJson = buildConfigJson()
    if (dialogMode.value === 'create') {
      await createNotifyChannel({
        channelKind: activeKind.value,
        provider: form.provider,
        name: form.name.trim(),
        enabled: !!form.enabled,
        configJson
      })
      ElMessage.success(t('CreateSuccess'))
    } else {
      const patch = {}
      if (activeKind.value === 'EMAIL') {
        Object.assign(patch, {
          smtpHost: form.smtpHost,
          smtpPort: form.smtpPort,
          smtpSsl: !!form.smtpSsl,
          smtpUsername: form.smtpUsername,
          mailFrom: form.mailFrom,
          mailFromDisplay: form.mailFromDisplay
        })
        if (form.smtpPassword && String(form.smtpPassword).trim()) {
          patch.smtpPassword = String(form.smtpPassword).trim()
        }
      } else if (form.provider === 'ALIYUN') {
        Object.assign(patch, {
          smsAccessKeyId: form.smsAccessKeyId,
          smsSignName: form.smsSignName,
          smsTemplateCode: form.smsTemplateCode,
          smsRegionId: form.smsRegionId
        })
        if (form.smsAccessKeySecret && String(form.smsAccessKeySecret).trim()) {
          patch.smsAccessKeySecret = String(form.smsAccessKeySecret).trim()
        }
      } else if (form.provider === 'TWILIO') {
        Object.assign(patch, {
          twilioAccountSid: String(form.twilioAccountSid || '').trim(),
          twilioFrom: String(form.twilioFrom || '').trim(),
          twilioMessagingServiceSid: String(form.twilioMessagingServiceSid || '').trim()
        })
        if (form.twilioAuthToken && String(form.twilioAuthToken).trim()) {
          patch.twilioAuthToken = String(form.twilioAuthToken).trim()
        }
      } else if (form.provider === 'AWS_SNS') {
        Object.assign(patch, {
          awsAccessKeyId: String(form.awsAccessKeyId || '').trim(),
          awsRegion: String(form.awsRegion || 'us-east-1').trim()
        })
        if (form.awsSecretAccessKey && String(form.awsSecretAccessKey).trim()) {
          patch.awsSecretAccessKey = String(form.awsSecretAccessKey).trim()
        }
      }
      await updateNotifyChannel(editId.value, {
        name: form.name.trim(),
        enabled: !!form.enabled,
        configJson: JSON.stringify(patch)
      })
      ElMessage.success(t('UpdateSuccess'))
    }
    dialogVisible.value = false
    await load()
  } catch {
    /* http interceptor */
  } finally {
    saving.value = false
  }
}

function openTest(row) {
  testRow.value = row
  testEmail.value = ''
  testPhone.value = ''
  testVisible.value = true
}

function resetTest() {
  testRow.value = null
}

async function runTest() {
  if (!testRow.value) return
  testing.value = true
  try {
    await testNotifyChannel(testRow.value.id, {
      testEmail: testRow.value.channelKind === 'EMAIL' ? testEmail.value?.trim() || undefined : undefined,
      testPhone: testRow.value.channelKind === 'SMS' ? testPhone.value?.trim() || undefined : undefined
    })
    ElMessage.success(t('IntegrationTestOk'))
    testVisible.value = false
  } catch {
    /* http interceptor */
  } finally {
    testing.value = false
  }
}

async function toggleEnabled(row) {
  try {
    const d = await getNotifyChannel(row.id)
    const cfg =
      typeof d.configJson === 'string' ? d.configJson : d.configJson != null ? JSON.stringify(d.configJson) : '{}'
    await updateNotifyChannel(row.id, {
      name: (d.name || row.name || '').trim(),
      enabled: !row.enabled,
      configJson: cfg
    })
    ElMessage.success(t('UpdateSuccess'))
    await load()
  } catch {
    /* http interceptor */
  }
}

async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(t('DeleteConfirmLabel'), t('NoticeTitle'), { type: 'warning' })
    await deleteNotifyChannel(row.id)
    ElMessage.success(t('DeleteSuccess'))
    await load()
  } catch (e) {
    if (e !== 'cancel') {
      /* */
    }
  }
}

async function load() {
  try {
    const raw = await listNotifyChannels({
      channelKind: activeKind.value,
      pageNum: page.pageNum,
      pageSize: page.pageSize
    })
    const { records, total } = parsePage(raw)
    rows.value = records
    page.total = total
  } catch (e) {
    rows.value = []
    ElMessage.error(e?.message || t('RequestFailedNotice'))
  }
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.notify-channel-tabs {
  margin-bottom: 12px;
}
.notify-channel-tabs :deep(.el-tabs__content) {
  display: none;
}
.notify-channel-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.pagination-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>

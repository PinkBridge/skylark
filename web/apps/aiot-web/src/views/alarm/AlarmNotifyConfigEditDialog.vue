<template>
  <el-dialog
    :model-value="visible"
    :title="$t('EditTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 640px"
    @closed="onDialogClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item :label="t('AlarmNotifyRuleColumn')">
        <span>{{ ruleLabel }}</span>
      </el-form-item>
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" maxlength="128" show-word-limit />
      </el-form-item>
      <el-form-item :label="t('StatusLabel')" prop="enabled">
        <el-switch v-model="form.enabled" />
      </el-form-item>
      <el-form-item :label="t('AlarmNotifyEmailChannel')" prop="emailEnabled">
        <el-switch v-model="form.emailEnabled" />
      </el-form-item>
      <el-form-item
        v-if="form.emailEnabled"
        :label="t('AlarmNotifyEmailChannelLabel')"
        prop="emailNotifyChannelId"
        :required="true"
      >
        <el-select v-model="form.emailNotifyChannelId" filterable clearable style="width: 100%">
          <el-option v-for="c in emailChannelOptions" :key="c.id" :label="`${c.name} (#${c.id} · ${c.provider})`" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.emailEnabled" :label="t('AlarmNotifyToEmails')" prop="toEmails" :required="true">
        <el-input v-model="form.toEmails" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item :label="t('AlarmNotifySmsChannel')" prop="smsEnabled">
        <el-switch v-model="form.smsEnabled" />
      </el-form-item>
      <el-form-item
        v-if="form.smsEnabled"
        :label="t('AlarmNotifySmsChannelLabel')"
        prop="smsNotifyChannelId"
        :required="true"
      >
        <el-select v-model="form.smsNotifyChannelId" filterable clearable style="width: 100%">
          <el-option v-for="c in smsChannelOptions" :key="c.id" :label="`${c.name} (#${c.id} · ${c.provider})`" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.smsEnabled" :label="t('AlarmNotifyToMobiles')" prop="toMobiles" :required="true">
        <el-input v-model="form.toMobiles" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="props.onCancel()">{{ $t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getAlarmNotifyConfig, updateAlarmNotifyConfig } from '@/views/alarm/AlarmApi'
import { listNotifyChannelOptions } from '@/views/notify/NotifyApi'

const props = defineProps(['visible', 'row', 'ruleOptions', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const formRef = ref(null)
const configId = ref(null)
const emailChannelOptions = ref([])
const smsChannelOptions = ref([])
const form = reactive({
  ruleId: null,
  name: '',
  remark: '',
  enabled: true,
  emailEnabled: false,
  smsEnabled: false,
  emailNotifyChannelId: null,
  smsNotifyChannelId: null,
  toEmails: '',
  toMobiles: ''
})

const rules = {
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  emailNotifyChannelId: [
    {
      validator: (_, v, cb) => {
        if (form.emailEnabled && (v == null || v === '')) {
          cb(new Error(t('AlarmNotifyEmailChannelRequired')))
        } else {
          cb()
        }
      },
      trigger: ['change', 'blur']
    }
  ],
  smsNotifyChannelId: [
    {
      validator: (_, v, cb) => {
        if (form.smsEnabled && (v == null || v === '')) {
          cb(new Error(t('AlarmNotifySmsChannelRequired')))
        } else {
          cb()
        }
      },
      trigger: ['change', 'blur']
    }
  ],
  toEmails: [
    {
      validator: (_, v, cb) => {
        if (!form.emailEnabled) {
          cb()
          return
        }
        if (!(v && String(v).trim())) {
          cb(new Error(t('AlarmNotifyToEmailsRequired')))
          return
        }
        cb()
      },
      trigger: ['blur', 'change']
    }
  ],
  toMobiles: [
    {
      validator: (_, v, cb) => {
        if (!form.smsEnabled) {
          cb()
          return
        }
        if (!(v && String(v).trim())) {
          cb(new Error(t('AlarmNotifyToMobilesRequired')))
          return
        }
        cb()
      },
      trigger: ['blur', 'change']
    }
  ]
}

const ruleLabel = computed(() => {
  const id = form.ruleId
  const r = (props.ruleOptions || []).find((x) => x.id === id)
  if (r) return `${r.name} (#${r.id})`
  return id != null ? `#${id}` : '—'
})

async function loadChannelOptions() {
  try {
    emailChannelOptions.value = (await listNotifyChannelOptions('EMAIL')) || []
  } catch {
    emailChannelOptions.value = []
  }
  try {
    smsChannelOptions.value = (await listNotifyChannelOptions('SMS')) || []
  } catch {
    smsChannelOptions.value = []
  }
}

watch(
  () => [props.visible, props.row?.id],
  async ([visible, id]) => {
    if (visible && id != null) {
      configId.value = id
      await loadChannelOptions()
      try {
        const detail = await getAlarmNotifyConfig(id)
        form.ruleId = detail.ruleId
        form.name = detail.name || ''
        form.remark = detail.remark || ''
        form.enabled = !!detail.enabled
        form.emailEnabled = !!detail.emailEnabled
        form.smsEnabled = !!detail.smsEnabled
        form.emailNotifyChannelId = detail.emailNotifyChannelId ?? null
        form.smsNotifyChannelId = detail.smsNotifyChannelId ?? null
        form.toEmails = detail.toEmails || ''
        form.toMobiles = detail.toMobiles || ''
      } catch (e) {
        ElMessage.error(e?.message || t('RequestFailedNotice'))
      }
    }
  },
  { immediate: true }
)

function onDialogClosed() {
  formRef.value?.resetFields?.()
}

async function handleSubmit() {
  if (!configId.value) return
  try {
    await formRef.value?.validate?.()
    await updateAlarmNotifyConfig(configId.value, {
      name: form.name.trim(),
      remark: form.remark?.trim() || undefined,
      enabled: !!form.enabled,
      emailEnabled: !!form.emailEnabled,
      smsEnabled: !!form.smsEnabled,
      emailNotifyChannelId: form.emailEnabled ? form.emailNotifyChannelId : undefined,
      smsNotifyChannelId: form.smsEnabled ? form.smsNotifyChannelId : undefined,
      toEmails: form.emailEnabled ? form.toEmails?.trim() || undefined : undefined,
      toMobiles: form.smsEnabled ? form.toMobiles?.trim() || undefined : undefined
    })
    ElMessage.success(t('UpdateSuccess'))
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>

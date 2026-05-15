<template>
  <el-dialog
    :model-value="visible"
    :title="$t('CreateTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 640px"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item :label="t('AlarmNotifyRuleColumn')" prop="ruleId">
        <el-select v-model="form.ruleId" filterable :placeholder="t('AlarmRuleId')" style="width: 100%">
          <el-option v-for="r in ruleOptions" :key="r.id" :label="`${r.name} (#${r.id})`" :value="r.id" />
        </el-select>
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
        <el-input v-model="form.toEmails" type="textarea" :rows="2" :placeholder="t('AlarmNotifyToEmails')" />
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
        <el-input v-model="form.toMobiles" type="textarea" :rows="2" :placeholder="t('AlarmNotifyToMobiles')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">{{ $t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { createAlarmNotifyConfig } from '@/views/alarm/AlarmApi'
import { listNotifyChannelOptions } from '@/views/notify/NotifyApi'

const props = defineProps(['visible', 'ruleOptions', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const formRef = ref(null)
const emailChannelOptions = ref([])
const smsChannelOptions = ref([])
const form = reactive({
  ruleId: null,
  name: '',
  enabled: true,
  emailEnabled: false,
  smsEnabled: false,
  emailNotifyChannelId: null,
  smsNotifyChannelId: null,
  toEmails: '',
  toMobiles: ''
})

const rules = {
  ruleId: [{ required: true, message: t('AlarmNotifyRuleRequired'), trigger: 'change' }],
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

function defaults() {
  form.ruleId = null
  form.name = ''
  form.enabled = true
  form.emailEnabled = false
  form.smsEnabled = false
  form.emailNotifyChannelId = null
  form.smsNotifyChannelId = null
  form.toEmails = ''
  form.toMobiles = ''
}

watch(
  () => props.visible,
  (v) => {
    if (v) {
      defaults()
      loadChannelOptions()
    }
  }
)

function handleClosed() {
  formRef.value?.resetFields?.()
}

function handleCancel() {
  defaults()
  props.onCancel()
}

async function handleSubmit() {
  try {
    await formRef.value?.validate?.()
    await createAlarmNotifyConfig({
      ruleId: form.ruleId,
      name: form.name.trim(),
      enabled: !!form.enabled,
      emailEnabled: !!form.emailEnabled,
      smsEnabled: !!form.smsEnabled,
      emailNotifyChannelId: form.emailEnabled ? form.emailNotifyChannelId : undefined,
      smsNotifyChannelId: form.smsEnabled ? form.smsNotifyChannelId : undefined,
      toEmails: form.emailEnabled ? form.toEmails?.trim() || undefined : undefined,
      toMobiles: form.smsEnabled ? form.toMobiles?.trim() || undefined : undefined
    })
    ElMessage.success(t('CreateSuccess'))
    defaults()
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>

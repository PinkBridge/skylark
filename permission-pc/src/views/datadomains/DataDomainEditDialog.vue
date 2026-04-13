<template>
  <el-dialog
    :model-value="visible"
    :title="t('EditTitle')"
    align-center
    destroy-on-close
    :modal="false"
    modal-penetrable
    :show-close="false"
    style="max-width: 700px"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('IDLabel')" prop="id">
        <el-input v-model="form.id" :disabled="true" />
      </el-form-item>
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('CodeLabel')" prop="code">
        <el-input v-model="form.code" :placeholder="t('CodeLabel')" />
      </el-form-item>
      <el-form-item :label="t('TypeLabel')" prop="type">
        <el-select v-model="form.type" :placeholder="t('TypeLabel')" style="width: 100%;">
          <el-option label="ALL" value="ALL" />
          <el-option label="TENANT" value="TENANT" />
          <el-option label="ORG_ALL" value="ORG_ALL" />
          <el-option label="ORG_AND_CHILD" value="ORG_AND_CHILD" />
          <el-option label="ORG_ONLY" value="ORG_ONLY" />
          <el-option label="SELF" value="SELF" />
          <el-option label="CUSTOM" value="CUSTOM" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('ScopeValueLabel')" prop="scopeValue">
        <el-input
          v-model="form.scopeValue"
          type="textarea"
          :rows="3"
          :placeholder="t('ScopeValueLabel')"
        />
      </el-form-item>
      <el-form-item :label="t('CustomSqlLabel')" prop="customSql">
        <el-input
          v-model="form.customSql"
          type="textarea"
          :rows="3"
          :placeholder="t('CustomSqlLabel')"
        />
      </el-form-item>
      <el-form-item :label="t('DescriptionLabel')" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          :placeholder="t('DescriptionLabel')"
        />
      </el-form-item>
      <el-form-item :label="t('TenantIdLabel')" prop="tenantId">
        <el-input-number
          v-model="form.tenantId"
          :min="0"
          :placeholder="t('TenantIdLabel')"
          style="width: 100%;"
        />
      </el-form-item>
      <el-form-item :label="t('EnabledLabel')" prop="enabled">
        <el-switch
          v-model="form.enabled"
          :active-text="t('Yes')"
          :inactive-text="t('No')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="onSubmit">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getDataDomainById, updateDataDomainById } from '@/views/datadomains/DataDomainApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  id: '',
  name: '',
  code: '',
  type: 'ALL',
  scopeValue: '',
  customSql: '',
  description: '',
  tenantId: null,
  enabled: true
})

const rules = computed(() => {
  return {
    name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
    code: [{ required: true, message: t('CodeRequired'), trigger: 'blur' }],
    type: [{ required: true, message: t('TypeRequired'), trigger: 'change' }]
  }
})

/** 与后端 Boolean / 历史 0·1 兼容，供 el-switch（boolean）使用 */
function toEnabledBool(v) {
  if (v === true || v === 1 || v === '1') {
    return true
  }
  if (v === false || v === 0 || v === '0') {
    return false
  }
  return true
}

const fetchDataDomain = () => {
  if (props.row && props.row.id) {
    getDataDomainById(props.row.id)
      .then((response) => {
        const data = response.dataDomain || response || {}
        form.value = {
          id: data.id || '',
          name: data.name || '',
          code: data.code || '',
          type: data.type || 'ALL',
          scopeValue: data.scopeValue || '',
          customSql: data.customSql || '',
          description: data.description || '',
          tenantId: data.tenantId ?? null,
          enabled: toEnabledBool(data.enabled)
        }
      })
      .catch((error) => {
        console.error('Failed to get data domain information:', error)
        ElMessage.error(error.message || 'Failed to get data domain information')
      })
  }
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const payload = {
      name: form.value.name,
      code: form.value.code,
      type: form.value.type,
      scopeValue: form.value.scopeValue || '',
      customSql: form.value.customSql || '',
      description: form.value.description || '',
      tenantId: form.value.tenantId,
      enabled: !!form.value.enabled
    }
    updateDataDomainById(form.value.id, payload)
      .then(() => {
        props.onSubmit()
      })
      .catch((error) => {
        console.error('Update data domain failed:', error)
        ElMessage.error(error.message || 'Update data domain failed')
      })
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}

const onCancel = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  props.onCancel()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, id]) => {
    if (visible && id) {
      fetchDataDomain()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>



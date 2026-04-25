<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(v) => { if (!v) onCancel() }"
    @close="onCancel"
    :title="t('TenantInitializeTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 640px"
    :modal="false"
    modal-penetrable
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-divider content-position="left">{{ t('TenantInitOrgSectionTitle') }}</el-divider>
      <el-form-item :label="t('NameLabel')" prop="orgName">
        <el-input v-model="form.orgName" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('TypeLabel')" prop="orgType">
        <el-select v-model="form.orgType" filterable :placeholder="t('TypeRequired')" style="width: 100%">
          <el-option label="COMPANY" value="COMPANY">
            <span style="float:left">COMPANY</span>
            <span style="float:right;color:var(--el-text-color-secondary)">{{ t('OrgTypeCompany') }}</span>
          </el-option>
          <el-option label="DEPARTMENT" value="DEPARTMENT">
            <span style="float:left">DEPARTMENT</span>
            <span style="float:right;color:var(--el-text-color-secondary)">{{ t('OrgTypeDepartment') }}</span>
          </el-option>
          <el-option label="TEAM" value="TEAM">
            <span style="float:left">TEAM</span>
            <span style="float:right;color:var(--el-text-color-secondary)">{{ t('OrgTypeTeam') }}</span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-divider content-position="left">{{ t('TenantInitUserSectionTitle') }}</el-divider>
      <el-form-item :label="t('RoleLabel')" prop="roleId">
        <el-select v-model="form.roleId" filterable clearable :placeholder="t('RoleRequired')" style="width: 100%">
          <el-option v-for="r in roleOptions" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('UsernameLabel')" prop="username">
        <el-input v-model="form.username" :placeholder="t('UsernameLabel')" />
      </el-form-item>
      <el-form-item :label="t('PasswordLabel')" prop="password">
        <el-input v-model="form.password" type="password" show-password :placeholder="t('PasswordLabel')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="onCancel">{{ t('CancelButtonText') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { initializeTenant, getTenantRoles } from '@/views/tenants/TenantApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const props = defineProps(['visible', 'tenantId', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const submitting = ref(false)
const form = ref({
  orgName: '',
  orgType: 'COMPANY',
  roleId: null,
  username: '',
  password: ''
})

const roleOptions = ref([])

const rules = computed(() => ({
  orgName: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  orgType: [{ required: true, message: t('TypeRequired'), trigger: 'change' }],
  roleId: [{ required: true, message: t('RoleRequired'), trigger: 'change' }],
  username: [{ required: true, message: t('UsernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('PasswordRequired'), trigger: 'blur' }]
}))

const loadRoles = async () => {
  if (!props.tenantId) {
    roleOptions.value = []
    return
  }
  try {
    const res = await getTenantRoles(props.tenantId)
    roleOptions.value = Array.isArray(res) ? res : (res.data || res.list || [])
  } catch (e) {
    roleOptions.value = []
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value.orgName = ''
  form.value.orgType = 'COMPANY'
  form.value.roleId = null
  form.value.username = ''
  form.value.password = ''
}

const onSubmit = async () => {
  if (!formRef.value || !props.tenantId) return
  try {
    await formRef.value.validate()
    submitting.value = true
    await initializeTenant(props.tenantId, {
      org: { name: form.value.orgName, type: form.value.orgType },
      user: { roleId: form.value.roleId, username: form.value.username, password: form.value.password }
    })
    ElMessage.success(t('TenantInitializeSuccess'))
    resetForm()
    props.onSubmit && props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

const onCancel = () => {
  resetForm()
  props.onCancel && props.onCancel()
}

watch(() => [props.visible, props.tenantId], ([v, tid]) => {
  if (v && tid) {
    loadRoles()
  }
}, { immediate: true })
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>


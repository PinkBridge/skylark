<template>
  <el-dialog
    :model-value="visible"
    :title="t('CreateTenantAdminTitle')"
    align-center
    destroy-on-close
    :show-close="false"
    style="max-width: 520px"
    :modal="false"
    modal-penetrable
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
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
        <el-button type="primary" @click="onSubmit">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { createTenantAdmin, getTenantRoles } from '@/views/tenants/TenantApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const props = defineProps(['visible', 'tenantId', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({
  roleId: null,
  username: '',
  password: ''
})

const roleOptions = ref([])

const rules = computed(() => ({
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
  form.value.roleId = null
  form.value.username = ''
  form.value.password = ''
}

const onSubmit = async () => {
  if (!formRef.value || !props.tenantId) {
    return
  }
  try {
    await formRef.value.validate()
    await createTenantAdmin(props.tenantId, {
      roleId: form.value.roleId,
      username: form.value.username,
      password: form.value.password
    })
    ElMessage.success(t('CreateTenantAdminSuccess'))
    resetForm()
    props.onSubmit && props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
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

<template>
  <el-dialog :model-value="visible" :title="t('EditTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false" style="max-width: 600px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="t('IDLabel')" prop="id">
        <el-input v-model="form.id" :disabled="true" />
      </el-form-item>
      <el-form-item :label="t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="t('NameLabel')" />
      </el-form-item>
      <el-form-item :label="t('RemarkLabel')" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="t('RemarkLabel')" />
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
import { getRoleById, updateRoleById } from '@/views/roles/RoleApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])

const formRef = ref(null)
const form = ref({})

const rules = computed(() => {
  return {
    name: [
      { required: true, message: t('NameRequired'), trigger: 'blur' }
    ],
  }
})

const fetchRoleData = () => {
  if (props.row && props.row.id) {
    getRoleById(props.row.id).then(response => {
      form.value = response.role || response || {}
    }).catch(error => {
      console.error('Failed to get role information:', error)
      ElMessage.error(error.message || 'Failed to get role information')
    })
  }
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    const role = {
      name: form.value.name,
      remark: form.value.remark || '',
    }
    updateRoleById(form.value.id, role).then(() => {
      props.onSubmit()
    }).catch(error => {
      console.error('Update role failed:', error)
      ElMessage.error(error.message || 'Update role failed')
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
  ([visible, rowId]) => {
    if (visible && rowId) {
      fetchRoleData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>


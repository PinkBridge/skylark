<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onConfirm() }" @close="onConfirm" :title="t('DetailTitle')" align-center destroy-on-close :modal="false"
    modal-penetrable :show-close="false">
    <el-descriptions border>
      <el-descriptions-item :label="t('IDLabel')">{{ menuInfo.id }}</el-descriptions-item>
      <el-descriptions-item :label="t('NameLabel')">{{ menuInfo.displayName || menuInfo.name }}</el-descriptions-item>
      <el-descriptions-item :label="t('ParentNameLabel')">{{ menuInfo.parentMenu ? (menuInfo.parentMenu.name) : '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('PathLabel')">{{ menuInfo.path }}</el-descriptions-item>
      <el-descriptions-item :label="t('HiddenLabel')">
        {{ menuInfo.hidden == true ? t('Yes') : t('No') }}
      </el-descriptions-item>
      <el-descriptions-item :label="t('TypeLabel')">{{ menuInfo.type }}</el-descriptions-item>
      <el-descriptions-item :label="t('IconLabel')">
        <el-icon :size="20" >
          <component :is="menuInfo.icon" />
        </el-icon>
      </el-descriptions-item>
      <el-descriptions-item :label="t('SortLabel')">{{ menuInfo.sort }}</el-descriptions-item>
      <el-descriptions-item :label="t('PermLabel')">{{ menuInfo.permlabel }}</el-descriptions-item>
      <el-descriptions-item :label="t('ModuleKeyLabel')">{{ menuInfo.moduleKey }}</el-descriptions-item>
      <el-descriptions-item :label="t('AppCodeLabel')">{{ menuInfo.appCode || '-' }}</el-descriptions-item>
      <el-descriptions-item :label="t('CreatedAtLabel')">{{ menuInfo.createTime }}</el-descriptions-item>
      <el-descriptions-item :label="t('UpdatedAtLabel')">{{ menuInfo.updateTime }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="onConfirm">{{ t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getMenuById } from '@/views/menus/MenuApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm'])

const menuInfo = ref({})

const fetchMenuData = () => {
  if (props.row && props.row.id) {
    getMenuById(props.row.id).then(response => {
      menuInfo.value = response.menu || response || {}
    }).catch(error => {
      console.error('Failed to get menu information:', error)
      ElMessage.error(error.message || 'Failed to get menu information')
    })
  }
}

const onConfirm = () => {
  props.onConfirm()
}

watch(
  () => [props.visible, props.row?.id],
  ([visible, rowId]) => {
    if (visible && rowId) {
      fetchMenuData()
    }
  },
  { immediate: true }
)
</script>

<style scoped></style>


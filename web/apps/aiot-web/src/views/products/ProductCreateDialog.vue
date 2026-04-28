<template>
  <el-dialog :model-value="visible" :title="$t('CreateTitle')" align-center destroy-on-close :show-close="false" style="max-width: 700px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="$t('DeviceTypeLabel')" prop="deviceType">
        <div class="device-type-grid">
          <div
            v-for="item in deviceTypeCards"
            :key="item.value"
            class="device-type-card"
            :class="{ active: form.deviceType === item.value }"
            @click="form.deviceType = item.value"
          >
            <el-icon class="device-type-icon">
              <component :is="item.icon" />
            </el-icon>
            <div class="device-type-title">{{ $t(item.labelKey) }}</div>
            <div v-if="form.deviceType === item.value" class="device-type-check">✓</div>
          </div>
        </div>
      </el-form-item>
      <el-form-item :label="$t('ProductKeyLabel')" prop="productKey">
        <el-input v-model="form.productKey" :placeholder="$t('ProductKeyPlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('NameLabel')" prop="name">
        <el-input v-model="form.name" :placeholder="$t('NameRequired')" />
      </el-form-item>
      <el-form-item :label="$t('ProductCoverImageLabel')" prop="coverImageUrl">
        <ResourceUpload
          :file-list="form.coverImageUrl"
          :on-success="(url) => { form.coverImageUrl = url }"
        />
      </el-form-item>
      <el-form-item :label="$t('ProductThumbnailLabel')" prop="thumbnailUrl">
        <ResourceUpload
          :file-list="form.thumbnailUrl"
          :on-success="(url) => { form.thumbnailUrl = url }"
        />
      </el-form-item>
      <el-form-item :label="$t('DescriptionLabel')" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="4" :placeholder="$t('DescriptionPlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('ProductProtocolTypeLabel')" prop="protocolType">
        <el-select v-model="form.protocolType" style="width: 100%" :placeholder="$t('ProductProtocolTypeRequired')">
          <el-option
            v-for="item in PRODUCT_PROTOCOL_OPTIONS"
            :key="item.value"
            :label="$t(item.labelKey)"
            :value="item.value"
          />
        </el-select>
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
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Connection, Monitor, Share, VideoCamera } from '@element-plus/icons-vue'
import { createProduct } from '@/views/products/ProductApi'
import { PRODUCT_PROTOCOL_OPTIONS } from '@/views/products/productProtocolOptions'
import { DEVICE_TYPE_OPTIONS } from '@/views/devices/deviceTypeOptions'
import ResourceUpload from '@/components/ResourceUpload.vue'

const props = defineProps(['visible', 'onSubmit', 'onCancel'])
const { t } = useI18n()
const formRef = ref(null)
const form = ref({
  productKey: '',
  name: '',
  coverImageUrl: '',
  thumbnailUrl: '',
  description: '',
  protocolType: 'MQTT_ALINK_JSON',
  deviceType: 'DIRECT_DEVICE'
})

const rules = computed(() => ({
  productKey: [{ required: true, message: t('ProductKeyRequired'), trigger: 'blur' }],
  name: [{ required: true, message: t('NameRequired'), trigger: 'blur' }],
  protocolType: [{ required: true, message: t('ProductProtocolTypeRequired'), trigger: 'change' }],
  deviceType: [{ required: true, message: t('DeviceTypeRequired'), trigger: 'change' }]
}))

const deviceTypeIconMap = {
  DIRECT_DEVICE: Connection,
  GATEWAY_DEVICE: Monitor,
  GATEWAY_SUB_DEVICE: Share,
  CAMERA: VideoCamera
}

const deviceTypeCards = computed(() =>
  DEVICE_TYPE_OPTIONS.map((item) => ({
    ...item,
    icon: deviceTypeIconMap[item.value] || Connection
  }))
)

const resetForm = () => {
  form.value = { productKey: '', name: '', coverImageUrl: '', thumbnailUrl: '', description: '', protocolType: 'MQTT_ALINK_JSON', deviceType: 'DIRECT_DEVICE' }
  formRef.value?.resetFields()
}

const handleCancel = () => {
  resetForm()
  props.onCancel()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await createProduct(form.value)
    ElMessage.success(t('CreateSuccess'))
    resetForm()
    props.onSubmit()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  }
}
</script>

<style scoped>
.device-type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  width: 100%;
}

.device-type-card {
  position: relative;
  min-height: 108px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
  color: var(--el-text-color-secondary);
  padding: 12px 10px;
  overflow: hidden;
}

.device-type-card:hover {
  border-color: var(--el-color-primary-light-5);
}

.device-type-card.active {
  border-width: 2px;
  border-color: #21375b;
  color: #21375b;
}

.device-type-icon {
  font-size: 30px;
}

.device-type-title {
  font-size: 15px;
  line-height: 1.2;
  text-align: center;
}

.device-type-check {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding-right: 5px;
  padding-bottom: 3px;
  color: #fff;
  font-size: 13px;
  line-height: 1;
  background: linear-gradient(135deg, transparent 50%, #21375b 50%);
  border-bottom-right-radius: 4px;
}

.image-preview {
  width: 100%;
  max-width: 240px;
  height: 140px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  overflow: hidden;
}

.image-preview-small {
  max-width: 120px;
  height: 120px;
}

.image-preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>


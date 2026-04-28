<template>
  <el-dialog :model-value="visible" :title="$t('EditTitle')" align-center destroy-on-close :show-close="false" style="max-width: 700px">
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
        <el-input v-model="form.productKey" disabled />
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
        <el-button @click="props.onCancel()">{{ $t('CancelButtonText') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('ConfirmButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/* global defineProps */
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Connection, Monitor, Share, VideoCamera } from '@element-plus/icons-vue'
import { getProduct, updateProduct } from '@/views/products/ProductApi'
import { PRODUCT_PROTOCOL_OPTIONS } from '@/views/products/productProtocolOptions'
import { DEVICE_TYPE_OPTIONS } from '@/views/devices/deviceTypeOptions'
import ResourceUpload from '@/components/ResourceUpload.vue'

const props = defineProps(['visible', 'row', 'onSubmit', 'onCancel'])
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

watch(
  () => [props.visible, props.row?.productKey],
  async ([visible, productKey]) => {
    if (visible && productKey) {
      const data = await getProduct(productKey)
      form.value = {
        productKey: data.productKey || '',
        name: data.name || '',
        coverImageUrl: data.coverImageUrl || '',
        thumbnailUrl: data.thumbnailUrl || '',
        description: data.description || '',
        protocolType: data.protocolType || 'MQTT_ALINK_JSON',
        deviceType: data.deviceType || 'DIRECT_DEVICE'
      }
    }
  },
  { immediate: true }
)

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await updateProduct(form.value.productKey, {
      name: form.value.name,
      coverImageUrl: form.value.coverImageUrl,
      thumbnailUrl: form.value.thumbnailUrl,
      description: form.value.description,
      protocolType: form.value.protocolType,
      deviceType: form.value.deviceType
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
</style>


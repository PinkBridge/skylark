<template>
  <div class="product-detail-page">
    <el-card shadow="always">
      <div class="page-toolbar">
        <div class="page-title-wrap">
          <el-button class="back-button" text :icon="ArrowLeft" @click="router.push('/iot/products')" />
          <div class="page-title">{{ $t('ProductDetailTitle') }}</div>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">{{ $t('BasicInfoTitle') }}</div>
        <div class="image-block">
          <div class="image-item">
            <div class="image-label">{{ $t('ProductCoverImageLabel') }}</div>
            <el-image :src="detail.coverImageUrl" fit="cover" class="detail-image">
              <template #error>
                <div class="detail-image-placeholder">{{ $t('ProductNoImage') }}</div>
              </template>
            </el-image>
          </div>
          <div class="image-item">
            <div class="image-label">{{ $t('ProductThumbnailLabel') }}</div>
            <el-image :src="detail.thumbnailUrl" fit="cover" class="detail-image detail-image-small">
              <template #error>
                <div class="detail-image-placeholder">{{ $t('ProductNoImage') }}</div>
              </template>
            </el-image>
          </div>
        </div>
        <el-descriptions :column="3" border class="detail-descriptions">
          <el-descriptions-item :label="$t('ProductKeyLabel')">{{ detail.productKey || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ProductSecretLabel')">
            <span class="product-secret-row">
              <span class="product-secret-text">{{ displayProductSecret }}</span>
              <span v-if="detail.productSecret" class="product-secret-actions">
                <el-button
                  class="product-secret-icon-btn"
                  link
                  type="primary"
                  :icon="productSecretVisible ? Hide : View"
                  @click="productSecretVisible = !productSecretVisible"
                />
                <el-button
                  class="product-secret-icon-btn"
                  link
                  type="primary"
                  :icon="DocumentCopy"
                  :title="$t('CopyLabel')"
                  @click="copyProductSecret"
                />
              </span>
            </span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('NameLabel')">{{ detail.name || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ProductProtocolTypeLabel')">{{ detail.protocolType || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('DeviceTypeLabel')">{{ detail.deviceType || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('ProductDeviceCountLabel')">{{ detail.deviceCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item :label="$t('StatusLabel')">
            <el-tag :type="detail.status === 'enabled' ? 'success' : 'danger'">
              {{ detail.status === 'enabled' ? $t('Enabled') : $t('Disabled') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('DescriptionLabel')">{{ detail.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="detail-section">
        <div class="section-title">{{ $t('ProductDataChannelTitle') }}</div>
        <el-table :data="dataChannels" border stripe>
          <el-table-column prop="action" :label="$t('ProductDataChannelActionLabel')" width="120" />
          <el-table-column prop="topicPattern" :label="$t('ProductDataChannelTopicLabel')" min-width="360" show-overflow-tooltip />
          <el-table-column :label="$t('ProductDataChannelEffectLabel')" width="160">
            <template #default="{ row }">
              <el-select
                :model-value="row.effect"
                size="small"
                :loading="savingChannelId === row.id"
                :disabled="savingChannelId === row.id"
                @change="(value) => handleEffectChange(row, value)"
              >
                <el-option value="allow" :label="$t('AclEffectAllow')" />
                <el-option value="deny" :label="$t('AclEffectDeny')" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('ProductDataChannelEnabledLabel')" width="120" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.enabled"
                :loading="savingChannelId === row.id"
                :disabled="savingChannelId === row.id"
                @change="(value) => handleEnabledChange(row, value)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="priority" :label="$t('ProductDataChannelPriorityLabel')" width="120" />
          <template #empty>
            <el-empty :description="$t('ProductDataChannelNoData')" />
          </template>
        </el-table>
      </div>

      <div class="detail-section">
        <ThingModelPanel
          :product-key="detail.productKey"
          :protocol-type="detail.protocolType"
        />
      </div>

    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, DocumentCopy, Hide, View } from '@element-plus/icons-vue'
import { getProduct, getProductDataChannels, updateProductDataChannel } from '@/views/products/ProductApi'
import ThingModelPanel from '@/views/thing-model/ThingModelPanel.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const detail = ref({
  productKey: '',
  productSecret: '',
  name: '',
  coverImageUrl: '',
  thumbnailUrl: '',
  description: '',
  protocolType: '',
  deviceType: '',
  status: '',
  deviceCount: 0
})
const dataChannels = ref([])
const savingChannelId = ref(null)

const productSecretVisible = ref(false)

const displayProductSecret = computed(() => {
  const s = detail.value.productSecret
  if (!s) return '-'
  if (productSecretVisible.value) return s
  return '*'.repeat(s.length)
})

const loadDetail = async () => {
  const productKey = route.params.productKey
  if (!productKey) return
  try {
    const data = await getProduct(productKey)
    productSecretVisible.value = false
    detail.value = {
      productKey: data.productKey || '',
      productSecret: data.productSecret || '',
      name: data.name || '',
      coverImageUrl: data.coverImageUrl || '',
      thumbnailUrl: data.thumbnailUrl || '',
      description: data.description || '',
      protocolType: data.protocolType || '',
      deviceType: data.deviceType || '',
      status: data.status || '',
      deviceCount: data.deviceCount ?? 0
    }
    const channels = await getProductDataChannels(productKey)
    dataChannels.value = (channels || []).map((item) => ({
      id: item.id,
      action: item.action || '',
      topicPattern: item.topicPattern || '',
      effect: (item.effect || 'allow').toLowerCase(),
      enabled: Boolean(item.enabled),
      priority: item.priority ?? 0
    }))
  } catch (error) {
    ElMessage.error(error.message || 'Failed to load product detail')
  }
}

const saveDataChannel = async (row, payload) => {
  const productKey = detail.value.productKey
  if (!productKey || !row?.id) return
  try {
    savingChannelId.value = row.id
    await updateProductDataChannel(productKey, row.id, {
      effect: payload.effect,
      enabled: payload.enabled
    })
    ElMessage.success(t('ProductDataChannelUpdateSuccess'))
  } finally {
    savingChannelId.value = null
  }
}

const handleEffectChange = async (row, value) => {
  const prev = row.effect
  row.effect = value
  try {
    await saveDataChannel(row, {
      effect: row.effect,
      enabled: row.enabled
    })
  } catch (error) {
    row.effect = prev
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

const handleEnabledChange = async (row, value) => {
  const prev = row.enabled
  row.enabled = Boolean(value)
  try {
    await saveDataChannel(row, {
      effect: row.effect,
      enabled: row.enabled
    })
  } catch (error) {
    row.enabled = prev
    ElMessage.error(error.message || t('RequestFailedNotice'))
  }
}

const copyProductSecret = async () => {
  const text = detail.value.productSecret
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('ProductSecretCopied'))
  } catch {
    ElMessage.error(t('ProductSecretCopyFailed'))
  }
}

onMounted(loadDetail)

watch(() => route.params.productKey, loadDetail)
</script>

<style scoped>
.page-toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-button {
  padding: 8px;
  font-size: 18px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.detail-section + .detail-section {
  margin-top: 20px;
}

.image-block {
  margin-bottom: 16px;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.image-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.detail-image {
  width: 240px;
  height: 144px;
  border-radius: 10px;
  border: 1px solid var(--el-border-color-light);
  overflow: hidden;
}

.detail-image-small {
  width: 120px;
  height: 120px;
}

.detail-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.section-title {
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.detail-descriptions {
  background: #fff;
}

.detail-descriptions :deep(.el-descriptions__label.el-descriptions__cell) {
  background: #fafbfd;
}

.product-secret-row {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.product-secret-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 13px;
  line-height: 28px;
}

.product-secret-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  vertical-align: middle;
}

.product-secret-actions :deep(.product-secret-icon-btn.el-button.is-link) {
  margin: 0;
  padding: 0;
  width: 28px;
  height: 28px;
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.product-secret-actions :deep(.product-secret-icon-btn .el-icon) {
  font-size: 16px;
}
</style>


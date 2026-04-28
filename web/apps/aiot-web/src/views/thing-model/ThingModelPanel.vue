<template>
  <div class="thing-model-panel">
    <el-alert
      v-if="!isSupportedProtocol"
      :title="t('ThingModelProtocolEmptyNotice')"
      type="info"
      :closable="false"
      class="panel-alert"
    />

    <template v-else>
      <div class="panel-toolbar">
        <div class="toolbar-actions">
          <el-button class="toolbar-action-btn" :disabled="!productKey" @click="openJsonEditor">
            {{ t('ThingModelShowJsonLabel') }}
          </el-button>
          <el-button class="toolbar-action-btn" :icon="Refresh" :disabled="!productKey" @click="loadThingModel">{{ t('RefreshButtonLabel') }}</el-button>
        </div>
      </div>

      <template v-if="productKey">
        <div class="table-section model-section-card">
          <div class="table-header table-header-inline">
            <div class="table-header-left">
              <span class="table-title">{{ t('ThingModelPropertiesTitle') }}</span>
            </div>
            <el-button link type="primary" @click="startAdd('properties')">{{ t('AddLabel') }}</el-button>
          </div>
          <div v-if="parsedForm.properties.length" class="record-list property-record-list">
            <div v-for="(row, index) in parsedForm.properties" :key="`property-${row.identifier}-${index}`" class="record-card">
              <div class="record-card-header">
                <div class="record-title-group">
                  <div class="record-title">{{ row.name || '-' }}</div>
                  <div class="record-subtitle">{{ row.identifier || '-' }}</div>
                </div>
                <div class="record-actions">
                  <el-button link type="primary" @click="startEdit('properties', row, index)">{{ t('EditLabel') }}</el-button>
                  <el-button link type="primary" @click="removeItem('properties', index)">{{ t('DeleteLabel') }}</el-button>
                </div>
              </div>
              <div class="record-meta-grid">
                <div class="meta-item">
                  <span class="meta-label">{{ t('ThingModelDataTypeLabel') }}</span>
                  <span class="meta-value">{{ getDataTypeLabel(row.dataType) }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">{{ t('ThingModelAccessModeLabel') }}</span>
                  <span class="meta-value">{{ getAccessModeLabel(row.accessMode) }}</span>
                </div>
                <template v-if="isNumericPropertyType(row.dataType)">
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelMinValueLabel') }}</span>
                    <span class="meta-value">{{ row.minValue ?? '-' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelMaxValueLabel') }}</span>
                    <span class="meta-value">{{ row.maxValue ?? '-' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelStepLabel') }}</span>
                    <span class="meta-value">{{ row.step ?? '-' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelUnitLabel') }}</span>
                    <span class="meta-value">{{ row.unit || '-' }}</span>
                  </div>
                </template>
                <template v-if="row.dataType === 'bool'">
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelBoolTrueTextLabel') }}</span>
                    <span class="meta-value">{{ row.trueText || '-' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelBoolFalseTextLabel') }}</span>
                    <span class="meta-value">{{ row.falseText || '-' }}</span>
                  </div>
                </template>
                <template v-if="row.dataType === 'string'">
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelStringLengthLabel') }}</span>
                    <span class="meta-value">{{ row.length || '-' }}{{ row.length ? ` ${t('ThingModelStringLengthUnit')}` : '' }}</span>
                  </div>
                </template>
                <template v-if="row.dataType === 'enum'">
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelEnumValueTypeLabel') }}</span>
                    <span class="meta-value">{{ getDataTypeLabel(row.enumValueType) }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">{{ t('ThingModelEnumCountLabel') }}</span>
                    <span class="meta-value">{{ row.enumItems?.length || 0 }} {{ t('ThingModelEnumCountUnit') }}</span>
                  </div>
                  <div class="meta-item meta-item-full">
                    <div class="meta-label-row">
                      <span class="meta-label">{{ t('ThingModelEnumItemsLabel') }} ({{ row.enumItems?.length || 0 }})</span>
                      <el-button
                        v-if="(row.enumItems?.length || 0) > 2"
                        link
                        type="primary"
                        class="enum-toggle-button"
                        @click="openEnumPreview(row)"
                      >
                        {{ t('ThingModelExpandLabel') }}
                      </el-button>
                    </div>
                    <div class="enum-item-list">
                      <div
                        v-for="(enumItem, enumIndex) in getVisibleEnumItems(row.enumItems, false)"
                        :key="`enum-${index}-${enumIndex}`"
                        class="enum-item-row"
                      >
                        <span class="enum-item-value">{{ enumItem.value }}</span>
                        <span class="enum-item-description">{{ enumItem.description || '-' }}</span>
                      </div>
                    </div>
                  </div>
                </template>
                <div class="meta-item meta-item-full">
                  <span class="meta-label">{{ t('DescriptionLabel') }}</span>
                  <span class="meta-value">{{ row.description || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else :description="t('ThingModelEmptyNotice')" :image-size="72" />
        </div>

        <div class="table-section model-section-card">
          <div class="table-header table-header-inline">
            <div class="table-header-left">
              <span class="table-title">{{ t('ThingModelEventsTitle') }}</span>
            </div>
            <el-button link type="primary" @click="startAdd('events')">{{ t('AddLabel') }}</el-button>
          </div>
          <div v-if="parsedForm.events.length" class="record-list event-record-list">
            <div v-for="(row, index) in parsedForm.events" :key="`event-${row.identifier}-${index}`" class="record-card">
              <div class="record-card-header">
                <div class="record-title-group">
                  <div class="record-title">{{ row.name || '-' }}</div>
                  <div class="record-subtitle">{{ row.identifier || '-' }}</div>
                </div>
                <div class="record-actions">
                  <el-button link type="primary" @click="startEdit('events', row, index)">{{ t('EditLabel') }}</el-button>
                  <el-button link type="primary" @click="removeItem('events', index)">{{ t('DeleteLabel') }}</el-button>
                </div>
              </div>
              <div class="record-meta-grid">
                <div class="meta-item meta-item-full">
                  <span class="meta-label">{{ t('DescriptionLabel') }}</span>
                  <span class="meta-value">{{ row.description || '-' }}</span>
                </div>
              </div>
              <div class="nested-block">
                <div class="nested-title-row">
                  <div class="nested-title">{{ t('ThingModelOutputParamsLabel') }}</div>
                  <el-button
                    v-if="(row.outputParams?.length || 0) > 2"
                    link
                    type="primary"
                    class="enum-toggle-button"
                    @click="openParamPreview(`${t('ThingModelOutputParamsLabel')} - ${row.name || row.identifier || ''}`, row.outputParams)"
                  >
                    {{ t('ThingModelExpandLabel') }}
                  </el-button>
                </div>
                <div v-if="row.outputParams?.length" class="nested-list">
                  <div v-for="(param, paramIndex) in getVisibleEnumItems(row.outputParams, false)" :key="`event-output-${index}-${paramIndex}`" class="nested-item">
                    <div class="nested-item-header">
                      <span class="nested-name">{{ param.name || '-' }}</span>
                      <span class="nested-code">{{ param.identifier || '-' }}</span>
                    </div>
                    <div class="nested-item-meta">
                      <span>{{ t('ThingModelDataTypeLabel') }}: {{ getDataTypeLabel(param.dataType) }}</span>
                        <template v-if="isNumericPropertyType(param.dataType)">
                          <span>{{ t('ThingModelMinValueLabel') }}: {{ param.minValue ?? '-' }}</span>
                          <span>{{ t('ThingModelMaxValueLabel') }}: {{ param.maxValue ?? '-' }}</span>
                          <span>{{ t('ThingModelStepLabel') }}: {{ param.step ?? '-' }}</span>
                          <span>{{ t('ThingModelUnitLabel') }}: {{ param.unit || '-' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'bool'">
                          <span>{{ t('ThingModelBoolTrueTextLabel') }}: {{ param.trueText || '-' }}</span>
                          <span>{{ t('ThingModelBoolFalseTextLabel') }}: {{ param.falseText || '-' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'string'">
                          <span>{{ t('ThingModelStringLengthLabel') }}: {{ param.length || '-' }}{{ param.length ? ` ${t('ThingModelStringLengthUnit')}` : '' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'time'">
                          <span>{{ t('ThingModelTimeFormatLabel') }}: {{ t('ThingModelTimeFormatValue') }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'enum'">
                          <span>{{ t('ThingModelEnumValueTypeLabel') }}: {{ getDataTypeLabel(param.enumValueType) }}</span>
                          <span>{{ t('ThingModelEnumCountLabel') }}: {{ param.enumItems?.length || 0 }} {{ t('ThingModelEnumCountUnit') }}</span>
                        </template>
                      <span>{{ t('DescriptionLabel') }}: {{ param.description || '-' }}</span>
                    </div>
                      <div v-if="param.dataType === 'enum' && param.enumItems?.length" class="nested-enum-list">
                        <div
                          v-for="(enumItem, enumIndex) in getVisibleEnumItems(param.enumItems, false)"
                          :key="`event-output-enum-${index}-${paramIndex}-${enumIndex}`"
                          class="enum-item-row"
                        >
                          <span class="enum-item-value">{{ enumItem.value }}</span>
                          <span class="enum-item-description">{{ enumItem.description || '-' }}</span>
                        </div>
                        <el-button
                          v-if="param.enumItems.length > 2"
                          link
                          type="primary"
                          class="enum-toggle-button"
                          @click="openEnumPreview(param)"
                        >
                          {{ t('ThingModelExpandLabel') }}
                        </el-button>
                      </div>
                  </div>
                </div>
                <div v-else class="nested-empty">-</div>
              </div>
            </div>
          </div>
          <el-empty v-else :description="t('ThingModelEmptyNotice')" :image-size="72" />
        </div>

        <div class="table-section model-section-card">
          <div class="table-header table-header-inline">
            <div class="table-header-left">
              <span class="table-title">{{ t('ThingModelServicesTitle') }}</span>
            </div>
            <el-button link type="primary" @click="startAdd('services')">{{ t('AddLabel') }}</el-button>
          </div>
          <div v-if="parsedForm.services.length" class="record-list service-record-list">
            <div v-for="(row, index) in parsedForm.services" :key="`service-${row.identifier}-${index}`" class="record-card">
              <div class="record-card-header">
                <div class="record-title-group">
                  <div class="record-title">{{ row.name || '-' }}</div>
                  <div class="record-subtitle">{{ row.identifier || '-' }}</div>
                </div>
                <div class="record-actions">
                  <el-button link type="primary" @click="startEdit('services', row, index)">{{ t('EditLabel') }}</el-button>
                  <el-button link type="primary" @click="removeItem('services', index)">{{ t('DeleteLabel') }}</el-button>
                </div>
              </div>
              <div class="record-meta-grid">
                <div class="meta-item meta-item-full">
                  <span class="meta-label">{{ t('DescriptionLabel') }}</span>
                  <span class="meta-value">{{ row.description || '-' }}</span>
                </div>
              </div>
              <div class="nested-block-wrap">
                <div class="nested-block">
                  <div class="nested-title">{{ t('ThingModelInputParamsLabel') }}</div>
                  <div v-if="row.inputParams?.length" class="nested-list">
                    <div v-for="(param, paramIndex) in row.inputParams" :key="`service-input-${index}-${paramIndex}`" class="nested-item">
                      <div class="nested-item-header">
                        <span class="nested-name">{{ param.name || '-' }}</span>
                        <span class="nested-code">{{ param.identifier || '-' }}</span>
                      </div>
                      <div class="nested-item-meta">
                        <span>{{ t('ThingModelDataTypeLabel') }}: {{ getDataTypeLabel(param.dataType) }}</span>
                        <template v-if="isNumericPropertyType(param.dataType)">
                          <span>{{ t('ThingModelMinValueLabel') }}: {{ param.minValue ?? '-' }}</span>
                          <span>{{ t('ThingModelMaxValueLabel') }}: {{ param.maxValue ?? '-' }}</span>
                          <span>{{ t('ThingModelStepLabel') }}: {{ param.step ?? '-' }}</span>
                          <span>{{ t('ThingModelUnitLabel') }}: {{ param.unit || '-' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'bool'">
                          <span>{{ t('ThingModelBoolTrueTextLabel') }}: {{ param.trueText || '-' }}</span>
                          <span>{{ t('ThingModelBoolFalseTextLabel') }}: {{ param.falseText || '-' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'string'">
                          <span>{{ t('ThingModelStringLengthLabel') }}: {{ param.length || '-' }}{{ param.length ? ` ${t('ThingModelStringLengthUnit')}` : '' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'time'">
                          <span>{{ t('ThingModelTimeFormatLabel') }}: {{ t('ThingModelTimeFormatValue') }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'enum'">
                          <span>{{ t('ThingModelEnumValueTypeLabel') }}: {{ getDataTypeLabel(param.enumValueType) }}</span>
                          <span>{{ t('ThingModelEnumCountLabel') }}: {{ param.enumItems?.length || 0 }} {{ t('ThingModelEnumCountUnit') }}</span>
                        </template>
                        <span>{{ t('DescriptionLabel') }}: {{ param.description || '-' }}</span>
                      </div>
                      <div v-if="param.dataType === 'enum' && param.enumItems?.length" class="nested-enum-list">
                        <div
                          v-for="(enumItem, enumIndex) in getVisibleEnumItems(param.enumItems, false)"
                          :key="`service-input-enum-${index}-${paramIndex}-${enumIndex}`"
                          class="enum-item-row"
                        >
                          <span class="enum-item-value">{{ enumItem.value }}</span>
                          <span class="enum-item-description">{{ enumItem.description || '-' }}</span>
                        </div>
                        <el-button
                          v-if="param.enumItems.length > 2"
                          link
                          type="primary"
                          class="enum-toggle-button"
                          @click="openEnumPreview(param)"
                        >
                          {{ t('ThingModelExpandLabel') }}
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="nested-empty">-</div>
                </div>
                <div class="nested-block">
                  <div class="nested-title-row">
                    <div class="nested-title">{{ t('ThingModelOutputParamsLabel') }}</div>
                    <el-button
                      v-if="(row.outputParams?.length || 0) > 2"
                      link
                      type="primary"
                      class="enum-toggle-button"
                      @click="openParamPreview(`${t('ThingModelOutputParamsLabel')} - ${row.name || row.identifier || ''}`, row.outputParams)"
                    >
                      {{ t('ThingModelExpandLabel') }}
                    </el-button>
                  </div>
                  <div v-if="row.outputParams?.length" class="nested-list">
                    <div v-for="(param, paramIndex) in getVisibleEnumItems(row.outputParams, false)" :key="`service-output-${index}-${paramIndex}`" class="nested-item">
                      <div class="nested-item-header">
                        <span class="nested-name">{{ param.name || '-' }}</span>
                        <span class="nested-code">{{ param.identifier || '-' }}</span>
                      </div>
                      <div class="nested-item-meta">
                        <span>{{ t('ThingModelDataTypeLabel') }}: {{ getDataTypeLabel(param.dataType) }}</span>
                        <template v-if="isNumericPropertyType(param.dataType)">
                          <span>{{ t('ThingModelMinValueLabel') }}: {{ param.minValue ?? '-' }}</span>
                          <span>{{ t('ThingModelMaxValueLabel') }}: {{ param.maxValue ?? '-' }}</span>
                          <span>{{ t('ThingModelStepLabel') }}: {{ param.step ?? '-' }}</span>
                          <span>{{ t('ThingModelUnitLabel') }}: {{ param.unit || '-' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'bool'">
                          <span>{{ t('ThingModelBoolTrueTextLabel') }}: {{ param.trueText || '-' }}</span>
                          <span>{{ t('ThingModelBoolFalseTextLabel') }}: {{ param.falseText || '-' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'string'">
                          <span>{{ t('ThingModelStringLengthLabel') }}: {{ param.length || '-' }}{{ param.length ? ` ${t('ThingModelStringLengthUnit')}` : '' }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'time'">
                          <span>{{ t('ThingModelTimeFormatLabel') }}: {{ t('ThingModelTimeFormatValue') }}</span>
                        </template>
                        <template v-else-if="param.dataType === 'enum'">
                          <span>{{ t('ThingModelEnumValueTypeLabel') }}: {{ getDataTypeLabel(param.enumValueType) }}</span>
                          <span>{{ t('ThingModelEnumCountLabel') }}: {{ param.enumItems?.length || 0 }} {{ t('ThingModelEnumCountUnit') }}</span>
                        </template>
                        <span>{{ t('DescriptionLabel') }}: {{ param.description || '-' }}</span>
                      </div>
                      <div v-if="param.dataType === 'enum' && param.enumItems?.length" class="nested-enum-list">
                        <div
                          v-for="(enumItem, enumIndex) in getVisibleEnumItems(param.enumItems, false)"
                          :key="`service-output-enum-${index}-${paramIndex}-${enumIndex}`"
                          class="enum-item-row"
                        >
                          <span class="enum-item-value">{{ enumItem.value }}</span>
                          <span class="enum-item-description">{{ enumItem.description || '-' }}</span>
                        </div>
                        <el-button
                          v-if="param.enumItems.length > 2"
                          link
                          type="primary"
                          class="enum-toggle-button"
                          @click="openEnumPreview(param)"
                        >
                          {{ t('ThingModelExpandLabel') }}
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="nested-empty">-</div>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else :description="t('ThingModelEmptyNotice')" :image-size="72" />
        </div>

      </template>
    </template>

    <el-dialog
      :model-value="showJsonPreview"
      :title="t('ThingModelJsonPreviewTitle')"
      width="720px"
      destroy-on-close
      @close="cancelJsonEditor"
    >
      <el-input
        v-model="jsonDraft"
        type="textarea"
        :rows="22"
        class="json-editor"
        :spellcheck="false"
      />
      <template #footer>
        <div class="editor-actions">
          <el-button @click="cancelJsonEditor">{{ t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="submitJsonEditor">{{ t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      :model-value="enumPreviewVisible"
      :title="enumPreviewTitle"
      width="640px"
      destroy-on-close
      @close="closeEnumPreview"
    >
      <div class="enum-item-list">
        <div v-for="(enumItem, enumIndex) in enumPreviewItems" :key="`enum-preview-${enumIndex}`" class="enum-item-row">
          <span class="enum-item-value">{{ enumItem.value }}</span>
          <span class="enum-item-description">{{ enumItem.description || '-' }}</span>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      :model-value="paramPreviewVisible"
      :title="paramPreviewTitle"
      width="760px"
      destroy-on-close
      @close="closeParamPreview"
    >
      <div class="nested-list">
        <div v-for="(param, paramIndex) in paramPreviewItems" :key="`param-preview-${paramIndex}`" class="nested-item">
          <div class="nested-item-header">
            <span class="nested-name">{{ param.name || '-' }}</span>
            <span class="nested-code">{{ param.identifier || '-' }}</span>
          </div>
          <div class="nested-item-meta">
            <span>{{ t('ThingModelDataTypeLabel') }}: {{ getDataTypeLabel(param.dataType) }}</span>
            <template v-if="isNumericPropertyType(param.dataType)">
              <span>{{ t('ThingModelMinValueLabel') }}: {{ param.minValue ?? '-' }}</span>
              <span>{{ t('ThingModelMaxValueLabel') }}: {{ param.maxValue ?? '-' }}</span>
              <span>{{ t('ThingModelStepLabel') }}: {{ param.step ?? '-' }}</span>
              <span>{{ t('ThingModelUnitLabel') }}: {{ param.unit || '-' }}</span>
            </template>
            <template v-else-if="param.dataType === 'bool'">
              <span>{{ t('ThingModelBoolTrueTextLabel') }}: {{ param.trueText || '-' }}</span>
              <span>{{ t('ThingModelBoolFalseTextLabel') }}: {{ param.falseText || '-' }}</span>
            </template>
            <template v-else-if="param.dataType === 'string'">
              <span>{{ t('ThingModelStringLengthLabel') }}: {{ param.length || '-' }}{{ param.length ? ` ${t('ThingModelStringLengthUnit')}` : '' }}</span>
            </template>
            <template v-else-if="param.dataType === 'time'">
              <span>{{ t('ThingModelTimeFormatLabel') }}: {{ t('ThingModelTimeFormatValue') }}</span>
            </template>
            <template v-else-if="param.dataType === 'enum'">
              <span>{{ t('ThingModelEnumValueTypeLabel') }}: {{ getDataTypeLabel(param.enumValueType) }}</span>
              <span>{{ t('ThingModelEnumCountLabel') }}: {{ param.enumItems?.length || 0 }} {{ t('ThingModelEnumCountUnit') }}</span>
            </template>
            <span>{{ t('DescriptionLabel') }}: {{ param.description || '-' }}</span>
          </div>
          <div v-if="param.dataType === 'enum' && param.enumItems?.length" class="nested-enum-list">
            <div
              v-for="(enumItem, enumIndex) in param.enumItems"
              :key="`param-preview-enum-${paramIndex}-${enumIndex}`"
              class="enum-item-row"
            >
              <span class="enum-item-value">{{ enumItem.value }}</span>
              <span class="enum-item-description">{{ enumItem.description || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      :model-value="Boolean(activeSection)"
      :title="dialogTitle"
      align-center
      destroy-on-close
      :show-close="false"
      style="max-width: 980px"
    >
      <el-form
        ref="formRef"
        :model="draftItem"
        :rules="activeSection === 'properties' ? propertyFormRules : (activeSection === 'events' ? eventFormRules : (activeSection === 'services' ? serviceFormRules : undefined))"
        label-position="top"
        class="dialog-form"
      >
      <template v-if="activeSection === 'properties'">
          <el-form-item :label="t('ThingModelIdentifierLabel')" prop="identifier" class="field-item" required>
            <el-input v-model="draftItem.identifier" :placeholder="t('ThingModelIdentifierLabel')" />
          </el-form-item>
          <el-form-item :label="t('ThingModelLabelLabel')" prop="name" class="field-item" required>
            <el-input v-model="draftItem.name" :placeholder="t('ThingModelLabelLabel')" />
          </el-form-item>
          <el-form-item :label="t('ThingModelAccessModeLabel')" prop="accessMode" class="field-item" required>
            <el-radio-group v-model="draftItem.accessMode" class="data-type-group">
              <el-radio-button
                v-for="option in ACCESS_MODE_OPTIONS"
                :key="option.value"
                :label="option.value"
              >
                {{ t(option.labelKey) }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('ThingModelDataTypeLabel')" prop="dataType" class="field-item" required>
            <el-radio-group v-model="draftItem.dataType" class="data-type-group">
              <el-radio-button
                v-for="option in DATA_TYPE_OPTIONS"
                :key="option.value"
                :label="option.value"
              >
                {{ t(option.labelKey) }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <template v-if="isNumericPropertyType(draftItem.dataType)">
            <el-form-item :label="t('ThingModelRangeLabel')" prop="minValue" class="field-item" required>
              <div class="range-input-group">
                <el-input
                  v-model="draftItem.minValue"
                  :placeholder="t('ThingModelMinValueRequired')"
                  @blur="validatePropertyField('minValue')"
                />
                <span class="range-separator">~</span>
                <el-input
                  v-model="draftItem.maxValue"
                  :placeholder="t('ThingModelMaxValueRequired')"
                  @blur="validatePropertyField('minValue')"
                />
              </div>
            </el-form-item>
            <el-form-item :label="t('ThingModelStepLabel')" prop="step" class="field-item" required>
              <el-input v-model="draftItem.step" :placeholder="t('ThingModelStepRequired')" />
            </el-form-item>
            <el-form-item :label="t('ThingModelUnitLabel')" class="field-item">
              <el-input v-model="draftItem.unit" :placeholder="t('ThingModelUnitRequired')" />
            </el-form-item>
          </template>
          <template v-else-if="draftItem.dataType === 'time'">
            <el-form-item :label="t('ThingModelTimeFormatLabel')" class="field-item">
              <el-input :model-value="t('ThingModelTimeFormatValue')" disabled />
            </el-form-item>
          </template>
          <template v-else-if="draftItem.dataType === 'string'">
            <el-form-item :label="t('ThingModelStringLengthLabel')" prop="length" class="field-item" required>
              <el-input v-model="draftItem.length" :placeholder="t('ThingModelStringLengthPlaceholder')">
                <template #append>{{ t('ThingModelStringLengthUnit') }}</template>
              </el-input>
            </el-form-item>
          </template>
          <template v-else-if="draftItem.dataType === 'enum'">
            <el-form-item :label="t('ThingModelEnumValueTypeLabel')" prop="enumValueType" class="field-item" required>
              <el-radio-group v-model="draftItem.enumValueType" class="data-type-group">
                <el-radio-button
                  v-for="option in ENUM_VALUE_TYPE_OPTIONS"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ t(option.labelKey) }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="t('ThingModelEnumCountLabel')" prop="enumCount" class="field-item" required>
              <el-input
                v-model="draftItem.enumCount"
                :placeholder="t('ThingModelEnumCountPlaceholder')"
                @blur="normalizeEnumCount"
              >
                <template #append>{{ t('ThingModelEnumCountUnit') }}</template>
              </el-input>
            </el-form-item>
            <div v-if="draftItem.enumItems?.length" class="enum-form-list">
              <div
                v-for="(enumItem, enumIndex) in getVisibleEnumItems(draftItem.enumItems, enumItemsExpanded)"
                :key="`enum-item-${enumIndex}`"
                class="enum-form-row"
              >
                <el-form-item
                  :label="`${t('ThingModelEnumItemValueLabel')} ${enumIndex + 1}`"
                  :prop="`enumItems.${enumIndex}.value`"
                  :rules="[
                    { required: true, message: t('ThingModelEnumItemValueRequired'), trigger: 'blur' },
                    {
                      validator: (rule, value, callback) => {
                        if (!validateEnumItemValue(value)) {
                          callback(new Error(t('ThingModelEnumItemValueInvalid')))
                          return
                        }
                        callback()
                      },
                      trigger: ['blur', 'change']
                    }
                  ]"
                  class="field-item"
                  required
                >
                  <el-input v-model="enumItem.value" :placeholder="t('ThingModelEnumItemValueRequired')" />
                </el-form-item>
                <el-form-item
                  :label="`${t('ThingModelEnumItemDescriptionLabel')} ${enumIndex + 1}`"
                  :prop="`enumItems.${enumIndex}.description`"
                  :rules="[
                    { required: true, message: t('ThingModelEnumItemDescriptionRequired'), trigger: 'blur' }
                  ]"
                  class="field-item"
                  required
                >
                  <el-input v-model="enumItem.description" :placeholder="t('ThingModelEnumItemDescriptionRequired')" />
                </el-form-item>
              </div>
              <el-button
                v-if="draftItem.enumItems.length > 2"
                link
                type="primary"
                class="enum-toggle-button"
                @click="enumItemsExpanded = !enumItemsExpanded"
              >
                {{ enumItemsExpanded ? t('ThingModelCollapseLabel') : t('ThingModelExpandLabel') }}
              </el-button>
            </div>
          </template>
          <template v-else-if="draftItem.dataType === 'bool'">
            <el-form-item :label="t('ThingModelBoolTrueTextLabel')" prop="trueText" class="field-item" required>
              <el-input v-model="draftItem.trueText" :placeholder="t('ThingModelBoolTrueTextRequired')" />
            </el-form-item>
            <el-form-item :label="t('ThingModelBoolFalseTextLabel')" prop="falseText" class="field-item" required>
              <el-input v-model="draftItem.falseText" :placeholder="t('ThingModelBoolFalseTextRequired')" />
            </el-form-item>
          </template>
          <el-form-item :label="t('DescriptionLabel')" class="field-item">
            <el-input v-model="draftItem.description" :placeholder="t('DescriptionPlaceholder')" />
          </el-form-item>
      </template>

      <template v-else-if="activeSection === 'events'">
          <el-form-item :label="t('ThingModelIdentifierLabel')" prop="identifier" class="field-item" required>
            <el-input v-model="draftItem.identifier" :placeholder="t('ThingModelIdentifierLabel')" />
          </el-form-item>
          <el-form-item :label="t('ThingModelLabelLabel')" prop="name" class="field-item" required>
            <el-input v-model="draftItem.name" :placeholder="t('ThingModelLabelLabel')" />
          </el-form-item>
          <el-form-item :label="t('DescriptionLabel')" class="field-item">
            <el-input v-model="draftItem.description" :placeholder="t('DescriptionPlaceholder')" />
          </el-form-item>
        <div class="param-block">
          <div class="param-header">
            <span>{{ t('ThingModelOutputParamsLabel') }}</span>
          </div>
          <div v-for="(param, paramIndex) in draftItem.outputParams" :key="`event-param-${paramIndex}`" class="param-card">
            <div class="param-card-header">
              <span>{{ t('ThingModelParameterLabel') }} {{ paramIndex + 1 }}</span>
              <el-button link type="danger" @click="draftItem.outputParams.splice(paramIndex, 1)">{{ t('DeleteLabel') }}</el-button>
            </div>
            <div class="param-form">
              <el-form-item :label="t('ThingModelIdentifierLabel')" :prop="`outputParams.${paramIndex}.identifier`" :rules="getParamIdentifierRules(draftItem.outputParams)" class="field-item" required>
                <el-input v-model="param.identifier" :placeholder="t('ThingModelIdentifierLabel')" />
              </el-form-item>
              <el-form-item :label="t('ThingModelLabelLabel')" :prop="`outputParams.${paramIndex}.name`" :rules="getParamNameRules()" class="field-item" required>
                <el-input v-model="param.name" :placeholder="t('ThingModelLabelLabel')" />
              </el-form-item>
              <el-form-item :label="t('ThingModelDataTypeLabel')" :prop="`outputParams.${paramIndex}.dataType`" :rules="getParamDataTypeRules()" class="field-item">
                <el-radio-group v-model="param.dataType" class="data-type-group">
                  <el-radio-button
                    v-for="option in DATA_TYPE_OPTIONS"
                    :key="option.value"
                    :label="option.value"
                  >
                    {{ t(option.labelKey) }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <template v-if="isNumericPropertyType(param.dataType)">
                <el-form-item :label="t('ThingModelRangeLabel')" :prop="`outputParams.${paramIndex}.minValue`" :rules="getParamMinValueRules(param)" class="field-item" required>
                  <div class="range-input-group">
                    <el-input v-model="param.minValue" :placeholder="t('ThingModelMinValueRequired')" />
                    <span class="range-separator">~</span>
                    <el-input v-model="param.maxValue" :placeholder="t('ThingModelMaxValueRequired')" />
                  </div>
                </el-form-item>
                <el-form-item :label="t('ThingModelStepLabel')" :prop="`outputParams.${paramIndex}.step`" :rules="getParamStepRules(param)" class="field-item" required>
                  <el-input v-model="param.step" :placeholder="t('ThingModelStepRequired')" />
                </el-form-item>
                <el-form-item :label="t('ThingModelUnitLabel')" class="field-item">
                  <el-input v-model="param.unit" :placeholder="t('ThingModelUnitRequired')" />
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'time'">
                <el-form-item :label="t('ThingModelTimeFormatLabel')" class="field-item">
                  <el-input :model-value="t('ThingModelTimeFormatValue')" disabled />
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'string'">
                <el-form-item :label="t('ThingModelStringLengthLabel')" :prop="`outputParams.${paramIndex}.length`" :rules="getParamStringLengthRules(param)" class="field-item" required>
                  <el-input v-model="param.length" :placeholder="t('ThingModelStringLengthPlaceholder')">
                    <template #append>{{ t('ThingModelStringLengthUnit') }}</template>
                  </el-input>
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'enum'">
                <el-form-item :label="t('ThingModelEnumValueTypeLabel')" :prop="`outputParams.${paramIndex}.enumValueType`" :rules="getParamEnumValueTypeRules(param)" class="field-item" required>
                  <el-radio-group v-model="param.enumValueType" class="data-type-group">
                    <el-radio-button
                      v-for="option in ENUM_VALUE_TYPE_OPTIONS"
                      :key="option.value"
                      :label="option.value"
                    >
                      {{ t(option.labelKey) }}
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item :label="t('ThingModelEnumCountLabel')" :prop="`outputParams.${paramIndex}.enumCount`" :rules="getParamEnumCountRules(param)" class="field-item" required>
                  <el-input v-model="param.enumCount" :placeholder="t('ThingModelEnumCountPlaceholder')" @blur="normalizeEventParamEnumCount(param)">
                    <template #append>{{ t('ThingModelEnumCountUnit') }}</template>
                  </el-input>
                </el-form-item>
                <div v-if="param.enumItems?.length" class="enum-form-list">
                  <div v-for="(enumItem, enumIndex) in getVisibleEnumItems(param.enumItems, true)" :key="`event-param-enum-${paramIndex}-${enumIndex}`" class="enum-form-row">
                    <el-form-item :label="`${t('ThingModelEnumItemValueLabel')} ${enumIndex + 1}`" :prop="`outputParams.${paramIndex}.enumItems.${enumIndex}.value`" :rules="getParamEnumItemValueRules(param)" class="field-item" required>
                      <el-input v-model="enumItem.value" :placeholder="t('ThingModelEnumItemValueRequired')" />
                    </el-form-item>
                    <el-form-item :label="`${t('ThingModelEnumItemDescriptionLabel')} ${enumIndex + 1}`" :prop="`outputParams.${paramIndex}.enumItems.${enumIndex}.description`" :rules="getParamEnumItemDescriptionRules()" class="field-item" required>
                      <el-input v-model="enumItem.description" :placeholder="t('ThingModelEnumItemDescriptionRequired')" />
                    </el-form-item>
                  </div>
                </div>
              </template>
              <template v-else-if="param.dataType === 'bool'">
                <el-form-item :label="t('ThingModelBoolTrueTextLabel')" :prop="`outputParams.${paramIndex}.trueText`" :rules="getParamBoolTextRules('trueText')" class="field-item" required>
                  <el-input v-model="param.trueText" :placeholder="t('ThingModelBoolTrueTextRequired')" />
                </el-form-item>
                <el-form-item :label="t('ThingModelBoolFalseTextLabel')" :prop="`outputParams.${paramIndex}.falseText`" :rules="getParamBoolTextRules('falseText')" class="field-item" required>
                  <el-input v-model="param.falseText" :placeholder="t('ThingModelBoolFalseTextRequired')" />
                </el-form-item>
              </template>
              <el-form-item :label="t('DescriptionLabel')" class="field-item">
                <el-input v-model="param.description" :placeholder="t('DescriptionPlaceholder')" />
              </el-form-item>
            </div>
          </div>
          <div class="param-footer">
            <el-button link type="primary" @click="addEventOutputParam">{{ t('ThingModelContinueAddOutput') }}</el-button>
          </div>
        </div>
      </template>

      <template v-else-if="activeSection === 'services'">
          <el-form-item :label="t('ThingModelIdentifierLabel')" prop="identifier" class="field-item" required>
            <el-input v-model="draftItem.identifier" :placeholder="t('ThingModelIdentifierLabel')" />
          </el-form-item>
          <el-form-item :label="t('ThingModelLabelLabel')" prop="name" class="field-item" required>
            <el-input v-model="draftItem.name" :placeholder="t('ThingModelLabelLabel')" />
          </el-form-item>
          <el-form-item :label="t('DescriptionLabel')" class="field-item">
            <el-input v-model="draftItem.description" :placeholder="t('DescriptionPlaceholder')" />
          </el-form-item>
        <div class="param-block">
          <div class="param-header">
            <span>{{ t('ThingModelInputParamsLabel') }}</span>
          </div>
          <div v-for="(param, paramIndex) in draftItem.inputParams" :key="`service-in-${paramIndex}`" class="param-card">
            <div class="param-card-header">
              <span>{{ t('ThingModelParameterLabel') }} {{ paramIndex + 1 }}</span>
              <el-button link type="danger" @click="draftItem.inputParams.splice(paramIndex, 1)">{{ t('DeleteLabel') }}</el-button>
            </div>
            <div class="param-form">
              <el-form-item :label="t('ThingModelIdentifierLabel')" :prop="`inputParams.${paramIndex}.identifier`" :rules="getParamIdentifierRules(draftItem.inputParams)" class="field-item" required>
                <el-input v-model="param.identifier" :placeholder="t('ThingModelIdentifierLabel')" />
              </el-form-item>
              <el-form-item :label="t('ThingModelLabelLabel')" :prop="`inputParams.${paramIndex}.name`" :rules="getParamNameRules()" class="field-item" required>
                <el-input v-model="param.name" :placeholder="t('ThingModelLabelLabel')" />
              </el-form-item>
              <el-form-item :label="t('ThingModelDataTypeLabel')" :prop="`inputParams.${paramIndex}.dataType`" :rules="getParamDataTypeRules()" class="field-item">
                <el-radio-group v-model="param.dataType" class="data-type-group">
                  <el-radio-button
                    v-for="option in DATA_TYPE_OPTIONS"
                    :key="option.value"
                    :label="option.value"
                  >
                    {{ t(option.labelKey) }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <template v-if="isNumericPropertyType(param.dataType)">
                <el-form-item :label="t('ThingModelRangeLabel')" :prop="`inputParams.${paramIndex}.minValue`" :rules="getParamMinValueRules(param)" class="field-item" required>
                  <div class="range-input-group">
                    <el-input v-model="param.minValue" :placeholder="t('ThingModelMinValueRequired')" />
                    <span class="range-separator">~</span>
                    <el-input v-model="param.maxValue" :placeholder="t('ThingModelMaxValueRequired')" />
                  </div>
                </el-form-item>
                <el-form-item :label="t('ThingModelStepLabel')" :prop="`inputParams.${paramIndex}.step`" :rules="getParamStepRules(param)" class="field-item" required>
                  <el-input v-model="param.step" :placeholder="t('ThingModelStepRequired')" />
                </el-form-item>
                <el-form-item :label="t('ThingModelUnitLabel')" class="field-item">
                  <el-input v-model="param.unit" :placeholder="t('ThingModelUnitRequired')" />
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'time'">
                <el-form-item :label="t('ThingModelTimeFormatLabel')" class="field-item">
                  <el-input :model-value="t('ThingModelTimeFormatValue')" disabled />
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'string'">
                <el-form-item :label="t('ThingModelStringLengthLabel')" :prop="`inputParams.${paramIndex}.length`" :rules="getParamStringLengthRules(param)" class="field-item" required>
                  <el-input v-model="param.length" :placeholder="t('ThingModelStringLengthPlaceholder')">
                    <template #append>{{ t('ThingModelStringLengthUnit') }}</template>
                  </el-input>
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'enum'">
                <el-form-item :label="t('ThingModelEnumValueTypeLabel')" :prop="`inputParams.${paramIndex}.enumValueType`" :rules="getParamEnumValueTypeRules(param)" class="field-item" required>
                  <el-radio-group v-model="param.enumValueType" class="data-type-group">
                    <el-radio-button
                      v-for="option in ENUM_VALUE_TYPE_OPTIONS"
                      :key="option.value"
                      :label="option.value"
                    >
                      {{ t(option.labelKey) }}
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item :label="t('ThingModelEnumCountLabel')" :prop="`inputParams.${paramIndex}.enumCount`" :rules="getParamEnumCountRules(param)" class="field-item" required>
                  <el-input v-model="param.enumCount" :placeholder="t('ThingModelEnumCountPlaceholder')" @blur="normalizeEventParamEnumCount(param)">
                    <template #append>{{ t('ThingModelEnumCountUnit') }}</template>
                  </el-input>
                </el-form-item>
                <div v-if="param.enumItems?.length" class="enum-form-list">
                  <div v-for="(enumItem, enumIndex) in getVisibleEnumItems(param.enumItems, true)" :key="`service-input-enum-${paramIndex}-${enumIndex}`" class="enum-form-row">
                    <el-form-item :label="`${t('ThingModelEnumItemValueLabel')} ${enumIndex + 1}`" :prop="`inputParams.${paramIndex}.enumItems.${enumIndex}.value`" :rules="getParamEnumItemValueRules(param)" class="field-item" required>
                      <el-input v-model="enumItem.value" :placeholder="t('ThingModelEnumItemValueRequired')" />
                    </el-form-item>
                    <el-form-item :label="`${t('ThingModelEnumItemDescriptionLabel')} ${enumIndex + 1}`" :prop="`inputParams.${paramIndex}.enumItems.${enumIndex}.description`" :rules="getParamEnumItemDescriptionRules()" class="field-item" required>
                      <el-input v-model="enumItem.description" :placeholder="t('ThingModelEnumItemDescriptionRequired')" />
                    </el-form-item>
                  </div>
                </div>
              </template>
              <template v-else-if="param.dataType === 'bool'">
                <el-form-item :label="t('ThingModelBoolTrueTextLabel')" :prop="`inputParams.${paramIndex}.trueText`" :rules="getParamBoolTextRules('trueText')" class="field-item" required>
                  <el-input v-model="param.trueText" :placeholder="t('ThingModelBoolTrueTextRequired')" />
                </el-form-item>
                <el-form-item :label="t('ThingModelBoolFalseTextLabel')" :prop="`inputParams.${paramIndex}.falseText`" :rules="getParamBoolTextRules('falseText')" class="field-item" required>
                  <el-input v-model="param.falseText" :placeholder="t('ThingModelBoolFalseTextRequired')" />
                </el-form-item>
              </template>
              <el-form-item :label="t('DescriptionLabel')" class="field-item">
                <el-input v-model="param.description" :placeholder="t('DescriptionPlaceholder')" />
              </el-form-item>
            </div>
          </div>
          <div class="param-footer">
            <el-button link type="primary" @click="addServiceInputParam">{{ t('ThingModelContinueAddInput') }}</el-button>
          </div>
        </div>
        <div class="param-block">
          <div class="param-header">
            <span>{{ t('ThingModelOutputParamsLabel') }}</span>
          </div>
          <div v-for="(param, paramIndex) in draftItem.outputParams" :key="`service-out-${paramIndex}`" class="param-card">
            <div class="param-card-header">
              <span>{{ t('ThingModelParameterLabel') }} {{ paramIndex + 1 }}</span>
              <el-button link type="danger" @click="draftItem.outputParams.splice(paramIndex, 1)">{{ t('DeleteLabel') }}</el-button>
            </div>
            <div class="param-form">
              <el-form-item :label="t('ThingModelIdentifierLabel')" :prop="`outputParams.${paramIndex}.identifier`" :rules="getParamIdentifierRules(draftItem.outputParams)" class="field-item" required>
                <el-input v-model="param.identifier" :placeholder="t('ThingModelIdentifierLabel')" />
              </el-form-item>
              <el-form-item :label="t('ThingModelLabelLabel')" :prop="`outputParams.${paramIndex}.name`" :rules="getParamNameRules()" class="field-item" required>
                <el-input v-model="param.name" :placeholder="t('ThingModelLabelLabel')" />
              </el-form-item>
              <el-form-item :label="t('ThingModelDataTypeLabel')" :prop="`outputParams.${paramIndex}.dataType`" :rules="getParamDataTypeRules()" class="field-item">
                <el-radio-group v-model="param.dataType" class="data-type-group">
                  <el-radio-button
                    v-for="option in DATA_TYPE_OPTIONS"
                    :key="option.value"
                    :label="option.value"
                  >
                    {{ t(option.labelKey) }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <template v-if="isNumericPropertyType(param.dataType)">
                <el-form-item :label="t('ThingModelRangeLabel')" :prop="`outputParams.${paramIndex}.minValue`" :rules="getParamMinValueRules(param)" class="field-item" required>
                  <div class="range-input-group">
                    <el-input v-model="param.minValue" :placeholder="t('ThingModelMinValueRequired')" />
                    <span class="range-separator">~</span>
                    <el-input v-model="param.maxValue" :placeholder="t('ThingModelMaxValueRequired')" />
                  </div>
                </el-form-item>
                <el-form-item :label="t('ThingModelStepLabel')" :prop="`outputParams.${paramIndex}.step`" :rules="getParamStepRules(param)" class="field-item" required>
                  <el-input v-model="param.step" :placeholder="t('ThingModelStepRequired')" />
                </el-form-item>
                <el-form-item :label="t('ThingModelUnitLabel')" class="field-item">
                  <el-input v-model="param.unit" :placeholder="t('ThingModelUnitRequired')" />
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'time'">
                <el-form-item :label="t('ThingModelTimeFormatLabel')" class="field-item">
                  <el-input :model-value="t('ThingModelTimeFormatValue')" disabled />
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'string'">
                <el-form-item :label="t('ThingModelStringLengthLabel')" :prop="`outputParams.${paramIndex}.length`" :rules="getParamStringLengthRules(param)" class="field-item" required>
                  <el-input v-model="param.length" :placeholder="t('ThingModelStringLengthPlaceholder')">
                    <template #append>{{ t('ThingModelStringLengthUnit') }}</template>
                  </el-input>
                </el-form-item>
              </template>
              <template v-else-if="param.dataType === 'enum'">
                <el-form-item :label="t('ThingModelEnumValueTypeLabel')" :prop="`outputParams.${paramIndex}.enumValueType`" :rules="getParamEnumValueTypeRules(param)" class="field-item" required>
                  <el-radio-group v-model="param.enumValueType" class="data-type-group">
                    <el-radio-button
                      v-for="option in ENUM_VALUE_TYPE_OPTIONS"
                      :key="option.value"
                      :label="option.value"
                    >
                      {{ t(option.labelKey) }}
                    </el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item :label="t('ThingModelEnumCountLabel')" :prop="`outputParams.${paramIndex}.enumCount`" :rules="getParamEnumCountRules(param)" class="field-item" required>
                  <el-input v-model="param.enumCount" :placeholder="t('ThingModelEnumCountPlaceholder')" @blur="normalizeEventParamEnumCount(param)">
                    <template #append>{{ t('ThingModelEnumCountUnit') }}</template>
                  </el-input>
                </el-form-item>
                <div v-if="param.enumItems?.length" class="enum-form-list">
                  <div v-for="(enumItem, enumIndex) in getVisibleEnumItems(param.enumItems, true)" :key="`service-output-enum-${paramIndex}-${enumIndex}`" class="enum-form-row">
                    <el-form-item :label="`${t('ThingModelEnumItemValueLabel')} ${enumIndex + 1}`" :prop="`outputParams.${paramIndex}.enumItems.${enumIndex}.value`" :rules="getParamEnumItemValueRules(param)" class="field-item" required>
                      <el-input v-model="enumItem.value" :placeholder="t('ThingModelEnumItemValueRequired')" />
                    </el-form-item>
                    <el-form-item :label="`${t('ThingModelEnumItemDescriptionLabel')} ${enumIndex + 1}`" :prop="`outputParams.${paramIndex}.enumItems.${enumIndex}.description`" :rules="getParamEnumItemDescriptionRules()" class="field-item" required>
                      <el-input v-model="enumItem.description" :placeholder="t('ThingModelEnumItemDescriptionRequired')" />
                    </el-form-item>
                  </div>
                </div>
              </template>
              <template v-else-if="param.dataType === 'bool'">
                <el-form-item :label="t('ThingModelBoolTrueTextLabel')" :prop="`outputParams.${paramIndex}.trueText`" :rules="getParamBoolTextRules('trueText')" class="field-item" required>
                  <el-input v-model="param.trueText" :placeholder="t('ThingModelBoolTrueTextRequired')" />
                </el-form-item>
                <el-form-item :label="t('ThingModelBoolFalseTextLabel')" :prop="`outputParams.${paramIndex}.falseText`" :rules="getParamBoolTextRules('falseText')" class="field-item" required>
                  <el-input v-model="param.falseText" :placeholder="t('ThingModelBoolFalseTextRequired')" />
                </el-form-item>
              </template>
              <el-form-item :label="t('DescriptionLabel')" class="field-item">
                <el-input v-model="param.description" :placeholder="t('DescriptionPlaceholder')" />
              </el-form-item>
            </div>
          </div>
          <div class="param-footer">
            <el-button link type="primary" @click="addServiceOutputParam">{{ t('ThingModelContinueAddOutput') }}</el-button>
          </div>
        </div>
      </template>
      </el-form>

      <template #footer>
        <div class="editor-actions">
          <el-button @click="cancelEdit">{{ t('CancelButtonText') }}</el-button>
          <el-button type="primary" @click="submitSectionEdit">{{ t('ConfirmButtonText') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/* global defineProps */
import { computed, nextTick, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getThingModel, saveThingModel } from '@/views/thing-model/ThingModelApi'
import {
  ACCESS_MODE_OPTIONS,
  DATA_TYPE_OPTIONS,
  buildThingModelJson,
  createEmptyThingModelForm,
  createEvent,
  createParam,
  createProperty,
  createService,
  parseThingModelJson
} from '@/views/thing-model/thingModelSchema'

const props = defineProps({
  productKey: {
    type: String,
    default: ''
  },
  protocolType: {
    type: String,
    default: ''
  }
})

const { t } = useI18n()
const formRef = ref(null)
const showJsonPreview = ref(false)
const enumPreviewVisible = ref(false)
const enumPreviewItems = ref([])
const enumPreviewTitle = ref('')
const paramPreviewVisible = ref(false)
const paramPreviewItems = ref([])
const paramPreviewTitle = ref('')
const jsonDraft = ref('{}')
const activeSection = ref('')
const editIndex = ref(-1)
const enumItemsExpanded = ref(false)
const thingModel = ref({
  version: '',
  modelJson: ''
})
const parsedForm = ref(createEmptyThingModelForm())
const draftItem = ref({})

const isSupportedProtocol = computed(() => props.protocolType === 'MQTT_ALINK_JSON')
const dialogTitle = computed(() => {
  if (activeSection.value === 'properties') {
    return editIndex.value > -1 ? `${t('EditLabel')}${t('ThingModelPropertiesTitle')}` : `${t('AddLabel')}${t('ThingModelPropertiesTitle')}`
  }
  if (activeSection.value === 'events') {
    return editIndex.value > -1 ? `${t('EditLabel')}${t('ThingModelEventsTitle')}` : `${t('AddLabel')}${t('ThingModelEventsTitle')}`
  }
  if (activeSection.value === 'services') {
    return editIndex.value > -1 ? `${t('EditLabel')}${t('ThingModelServicesTitle')}` : `${t('AddLabel')}${t('ThingModelServicesTitle')}`
  }
  return t('ThingModelEditorTitle')
})

const getOptionLabel = (options, value) => {
  const matched = options.find((option) => option.value === value)
  return matched ? t(matched.labelKey) : (value || '-')
}

const getDataTypeLabel = (value) => getOptionLabel(DATA_TYPE_OPTIONS, value)
const getAccessModeLabel = (value) => getOptionLabel(ACCESS_MODE_OPTIONS, value)
const isNumericPropertyType = (value) => value === 'int' || value === 'float'
const isBoolPropertyType = (value) => value === 'bool'
const ENUM_VALUE_TYPE_OPTIONS = DATA_TYPE_OPTIONS.filter((option) => ['int', 'float', 'string'].includes(option.value))

const normalizeEnumCount = () => {
  const rawCount = Number(draftItem.value.enumCount)
  if (!Number.isInteger(rawCount) || rawCount <= 0) {
    draftItem.value.enumItems = []
    return
  }
  const safeCount = Math.min(rawCount, 20)
  draftItem.value.enumCount = String(safeCount)
  const currentItems = Array.isArray(draftItem.value.enumItems) ? draftItem.value.enumItems : []
  draftItem.value.enumItems = Array.from({ length: safeCount }, (_, index) => ({
    value: currentItems[index]?.value ?? '',
    description: currentItems[index]?.description ?? ''
  }))
}

const normalizeEventParamEnumCount = (param) => {
  const rawCount = Number(param.enumCount)
  if (!Number.isInteger(rawCount) || rawCount <= 0) {
    param.enumItems = []
    return
  }
  const safeCount = Math.min(rawCount, 20)
  param.enumCount = String(safeCount)
  const currentItems = Array.isArray(param.enumItems) ? param.enumItems : []
  param.enumItems = Array.from({ length: safeCount }, (_, index) => ({
    value: currentItems[index]?.value ?? '',
    description: currentItems[index]?.description ?? ''
  }))
}

const getVisibleEnumItems = (items, expanded) => {
  if (!Array.isArray(items)) return []
  return expanded ? items : items.slice(0, 2)
}

const openEnumPreview = (row) => {
  enumPreviewTitle.value = `${t('ThingModelEnumItemsLabel')} - ${row.name || row.identifier || ''}`
  enumPreviewItems.value = row.enumItems || []
  enumPreviewVisible.value = true
}

const closeEnumPreview = () => {
  enumPreviewVisible.value = false
  enumPreviewItems.value = []
  enumPreviewTitle.value = ''
}

const openParamPreview = (title, items = []) => {
  paramPreviewTitle.value = title
  paramPreviewItems.value = items
  paramPreviewVisible.value = true
}

const closeParamPreview = () => {
  paramPreviewVisible.value = false
  paramPreviewItems.value = []
  paramPreviewTitle.value = ''
}

const validateEnumItemValue = (value, enumValueType = draftItem.value.enumValueType) => {
  if (enumValueType === 'int') {
    return Number.isInteger(Number(value))
  }
  if (enumValueType === 'float') {
    return value !== '' && value !== null && value !== undefined && !Number.isNaN(Number(value))
  }
  return Boolean(String(value ?? '').trim())
}

const getParamIdentifierRules = (params) => ([
  { required: true, message: t('ThingModelIdentifierRequired'), trigger: 'blur' },
  {
    validator: (rule, value, callback) => {
      const identifier = value?.trim()
      if (!identifier) return callback()
      const duplicatedCount = (params || []).filter((item) => item.identifier?.trim() === identifier).length
      if (duplicatedCount > 1) return callback(new Error(t('ThingModelIdentifierDuplicate')))
      callback()
    },
    trigger: 'blur'
  }
])

const getParamNameRules = () => ([
  { required: true, message: t('NameRequired'), trigger: 'blur' }
])

const getParamDataTypeRules = () => ([
  { required: true, message: t('ThingModelDataTypeRequired'), trigger: 'change' }
])

const getParamMinValueRules = (param) => ([
  {
    validator: (rule, value, callback) => {
      if (!isNumericPropertyType(param.dataType)) return callback()
      if (value === '' || value === null || value === undefined) return callback(new Error(t('ThingModelMinValueRequired')))
      const minValue = Number(value)
      const maxValue = Number(param.maxValue)
      if (Number.isNaN(minValue)) return callback(new Error(t('ThingModelMinValueRequired')))
      if (param.maxValue !== '' && !Number.isNaN(maxValue) && maxValue <= minValue) {
        return callback(new Error(t('ThingModelNumericRangeInvalid')))
      }
      callback()
    },
    trigger: ['blur', 'change']
  }
])

const getParamStepRules = (param) => ([
  {
    validator: (rule, value, callback) => {
      if (!isNumericPropertyType(param.dataType)) return callback()
      if (value === '' || value === null || value === undefined) return callback(new Error(t('ThingModelStepRequired')))
      const step = Number(value)
      if (Number.isNaN(step)) return callback(new Error(t('ThingModelStepRequired')))
      if (param.dataType === 'int' && !Number.isInteger(step)) return callback(new Error(t('ThingModelIntStepInvalid')))
      callback()
    },
    trigger: ['blur', 'change']
  }
])

const getParamBoolTextRules = (field) => ([
  {
    validator: (rule, value, callback) => {
      const text = value?.trim() || ''
      if (!text) return callback(new Error(t(field === 'trueText' ? 'ThingModelBoolTrueTextRequired' : 'ThingModelBoolFalseTextRequired')))
      if (text.length < 1 || text.length > 20) return callback(new Error(t('ThingModelBoolTextLengthInvalid')))
      callback()
    },
    trigger: ['blur', 'change']
  }
])

const getParamStringLengthRules = (param) => ([
  {
    validator: (rule, value, callback) => {
      if (param.dataType !== 'string') return callback()
      if (value === '' || value === null || value === undefined) return callback(new Error(t('ThingModelStringLengthRequired')))
      const length = Number(value)
      if (!Number.isInteger(length) || length <= 0) return callback(new Error(t('ThingModelStringLengthInvalid')))
      callback()
    },
    trigger: ['blur', 'change']
  }
])

const getParamEnumValueTypeRules = (param) => ([
  {
    validator: (rule, value, callback) => {
      if (param.dataType !== 'enum') return callback()
      if (!value) return callback(new Error(t('ThingModelDataTypeRequired')))
      callback()
    },
    trigger: 'change'
  }
])

const getParamEnumCountRules = (param) => ([
  {
    validator: (rule, value, callback) => {
      if (param.dataType !== 'enum') return callback()
      if (value === '' || value === null || value === undefined) return callback(new Error(t('ThingModelEnumCountRequired')))
      const count = Number(value)
      if (!Number.isInteger(count) || count <= 0) return callback(new Error(t('ThingModelEnumCountInvalid')))
      if (count > 20) return callback(new Error(t('ThingModelEnumCountMaxInvalid')))
      callback()
    },
    trigger: ['blur', 'change']
  }
])

const getParamEnumItemValueRules = (param) => ([
  { required: true, message: t('ThingModelEnumItemValueRequired'), trigger: 'blur' },
  {
    validator: (rule, value, callback) => {
      if (!validateEnumItemValue(value, param.enumValueType)) return callback(new Error(t('ThingModelEnumItemValueInvalid')))
      callback()
    },
    trigger: ['blur', 'change']
  }
])

const getParamEnumItemDescriptionRules = () => ([
  { required: true, message: t('ThingModelEnumItemDescriptionRequired'), trigger: 'blur' }
])

const eventFormRules = computed(() => ({
  identifier: [
    { required: true, message: t('ThingModelIdentifierRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const identifier = value?.trim()
        if (!identifier) return callback()
        const duplicated = (parsedForm.value.events || []).some((current, index) => index !== editIndex.value && current.identifier === identifier)
        if (duplicated) return callback(new Error(t('ThingModelIdentifierDuplicate')))
        callback()
      },
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: t('NameRequired'), trigger: 'blur' }
  ]
}))

const serviceFormRules = computed(() => ({
  identifier: [
    { required: true, message: t('ThingModelIdentifierRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const identifier = value?.trim()
        if (!identifier) return callback()
        const duplicated = (parsedForm.value.services || []).some((current, index) => index !== editIndex.value && current.identifier === identifier)
        if (duplicated) return callback(new Error(t('ThingModelIdentifierDuplicate')))
        callback()
      },
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: t('NameRequired'), trigger: 'blur' }
  ]
}))

const propertyFormRules = computed(() => ({
  identifier: [
    { required: true, message: t('ThingModelIdentifierRequired'), trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const identifier = value?.trim()
        if (!identifier) {
          callback()
          return
        }
        const duplicated = (parsedForm.value.properties || []).some((current, index) => {
          return index !== editIndex.value && current.identifier === identifier
        })
        if (duplicated) {
          callback(new Error(t('ThingModelIdentifierDuplicate')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: t('NameRequired'), trigger: 'blur' }
  ],
  accessMode: [
    { required: true, message: t('ThingModelAccessModeRequired'), trigger: 'change' }
  ],
  dataType: [
    { required: true, message: t('ThingModelDataTypeRequired'), trigger: 'change' }
  ],
  minValue: [
    {
      validator: (rule, value, callback) => {
        if (!isNumericPropertyType(draftItem.value.dataType)) {
          callback()
          return
        }
        if (value === '' || value === null || value === undefined) {
          callback(new Error(t('ThingModelMinValueRequired')))
          return
        }
        const minValue = Number(value)
        const maxValue = Number(draftItem.value.maxValue)
        if (Number.isNaN(minValue)) {
          callback(new Error(t('ThingModelMinValueRequired')))
          return
        }
        if (draftItem.value.maxValue !== '' && !Number.isNaN(maxValue) && maxValue <= minValue) {
          callback(new Error(t('ThingModelNumericRangeInvalid')))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  step: [
    {
      validator: (rule, value, callback) => {
        if (!isNumericPropertyType(draftItem.value.dataType)) {
          callback()
          return
        }
        if (value === '' || value === null || value === undefined) {
          callback(new Error(t('ThingModelStepRequired')))
          return
        }
        const step = Number(value)
        if (Number.isNaN(step)) {
          callback(new Error(t('ThingModelStepRequired')))
          return
        }
        if (draftItem.value.dataType === 'int' && !Number.isInteger(step)) {
          callback(new Error(t('ThingModelIntStepInvalid')))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  trueText: [
    {
      validator: (rule, value, callback) => {
        if (!isBoolPropertyType(draftItem.value.dataType)) {
          callback()
          return
        }
        const text = value?.trim() || ''
        if (!text) {
          callback(new Error(t('ThingModelBoolTrueTextRequired')))
          return
        }
        if (text.length < 1 || text.length > 20) {
          callback(new Error(t('ThingModelBoolTextLengthInvalid')))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  falseText: [
    {
      validator: (rule, value, callback) => {
        if (!isBoolPropertyType(draftItem.value.dataType)) {
          callback()
          return
        }
        const text = value?.trim() || ''
        if (!text) {
          callback(new Error(t('ThingModelBoolFalseTextRequired')))
          return
        }
        if (text.length < 1 || text.length > 20) {
          callback(new Error(t('ThingModelBoolTextLengthInvalid')))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  length: [
    {
      validator: (rule, value, callback) => {
        if (draftItem.value.dataType !== 'string') {
          callback()
          return
        }
        if (value === '' || value === null || value === undefined) {
          callback(new Error(t('ThingModelStringLengthRequired')))
          return
        }
        const length = Number(value)
        if (!Number.isInteger(length) || length <= 0) {
          callback(new Error(t('ThingModelStringLengthInvalid')))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  enumValueType: [
    {
      validator: (rule, value, callback) => {
        if (draftItem.value.dataType !== 'enum') {
          callback()
          return
        }
        if (!value) {
          callback(new Error(t('ThingModelDataTypeRequired')))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  enumCount: [
    {
      validator: (rule, value, callback) => {
        if (draftItem.value.dataType !== 'enum') {
          callback()
          return
        }
        if (value === '' || value === null || value === undefined) {
          callback(new Error(t('ThingModelEnumCountRequired')))
          return
        }
        const count = Number(value)
        if (!Number.isInteger(count) || count <= 0) {
          callback(new Error(t('ThingModelEnumCountInvalid')))
          return
        }
        if (count > 20) {
          callback(new Error(t('ThingModelEnumCountMaxInvalid')))
          return
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ]
}))

const createDraft = (section) => {
  if (section === 'properties') return createProperty()
  if (section === 'events') return createEvent()
  return createService()
}

const resetPanel = () => {
  thingModel.value = { version: '', modelJson: '' }
  parsedForm.value = createEmptyThingModelForm()
  showJsonPreview.value = false
  activeSection.value = ''
  editIndex.value = -1
  draftItem.value = {}
}

const syncParsedForm = () => {
  parsedForm.value = parseThingModelJson(thingModel.value.modelJson, thingModel.value.version)
}

const openJsonEditor = () => {
  try {
    jsonDraft.value = JSON.stringify(JSON.parse(thingModel.value.modelJson || '{}'), null, 2)
  } catch {
    jsonDraft.value = thingModel.value.modelJson || '{}'
  }
  showJsonPreview.value = true
}

const cancelJsonEditor = () => {
  showJsonPreview.value = false
}

const validatePropertyField = (prop) => {
  if (activeSection.value !== 'properties') return
  formRef.value?.validateField?.(prop).catch(() => {})
}

const addEventOutputParam = () => {
  if ((draftItem.value.outputParams?.length || 0) >= 50) {
    ElMessage.error(t('ThingModelEventOutputParamsMaxInvalid'))
    return
  }
  draftItem.value.outputParams.push(createParam())
}

const addServiceInputParam = () => {
  if ((draftItem.value.inputParams?.length || 0) >= 50) {
    ElMessage.error(t('ThingModelServiceInputParamsMaxInvalid'))
    return
  }
  draftItem.value.inputParams.push(createParam())
}

const addServiceOutputParam = () => {
  if ((draftItem.value.outputParams?.length || 0) >= 50) {
    ElMessage.error(t('ThingModelServiceOutputParamsMaxInvalid'))
    return
  }
  draftItem.value.outputParams.push(createParam())
}

const persistThingModel = async (nextState) => {
  const version = thingModel.value.version || nextState.version || 'v1'
  const payload = {
    ...nextState,
    version
  }
  const modelJson = buildThingModelJson(payload)
  await saveThingModel(props.productKey, {
    version,
    modelJson
  })
  thingModel.value = {
    version,
    modelJson
  }
  parsedForm.value = parseThingModelJson(modelJson, version)
  ElMessage.success(t('ThingModelSaveSuccess'))
}

const submitJsonEditor = async () => {
  let parsedJson
  try {
    parsedJson = JSON.parse(jsonDraft.value || '{}')
  } catch {
    ElMessage.error(t('ThingModelRawJsonInvalid'))
    return
  }

  const version = thingModel.value.version || 'v1'
  const modelJson = JSON.stringify(parsedJson, null, 2)
  await saveThingModel(props.productKey, {
    version,
    modelJson
  })
  thingModel.value = {
    version,
    modelJson
  }
  syncParsedForm()
  showJsonPreview.value = false
  ElMessage.success(t('ThingModelSaveSuccess'))
}

const validateSectionDraft = (section, item, currentIndex) => {
  const identifier = item.identifier?.trim()
  if (!identifier) {
    ElMessage.error(t('ThingModelIdentifierRequired'))
    return false
  }
  if (section === 'properties' && !item.name?.trim()) {
    ElMessage.error(t('NameRequired'))
    return false
  }
  if (section === 'properties' && !item.accessMode) {
    ElMessage.error(t('ThingModelAccessModeRequired'))
    return false
  }
  if (section === 'properties' && !item.dataType) {
    ElMessage.error(t('ThingModelDataTypeRequired'))
    return false
  }
  const list = parsedForm.value[section] || []
  const duplicated = list.some((current, index) => index !== currentIndex && current.identifier === identifier)
  if (duplicated) {
    ElMessage.error(t('ThingModelIdentifierDuplicate'))
    return false
  }
  if (section === 'properties' && isNumericPropertyType(item.dataType)) {
    if (item.minValue === '' || item.minValue === null || item.minValue === undefined) {
      ElMessage.error(t('ThingModelMinValueRequired'))
      return false
    }
    if (item.maxValue === '' || item.maxValue === null || item.maxValue === undefined) {
      ElMessage.error(t('ThingModelMaxValueRequired'))
      return false
    }
    if (item.step === '' || item.step === null || item.step === undefined) {
      ElMessage.error(t('ThingModelStepRequired'))
      return false
    }
    const minValue = Number(item.minValue)
    const maxValue = Number(item.maxValue)
    const step = Number(item.step)
    if (Number.isNaN(minValue) || Number.isNaN(maxValue) || Number.isNaN(step)) {
      ElMessage.error(t('ThingModelRawJsonInvalid'))
      return false
    }
    if (item.dataType === 'int' && !Number.isInteger(step)) {
      ElMessage.error(t('ThingModelIntStepInvalid'))
      return false
    }
    if (maxValue <= minValue) {
      ElMessage.error(t('ThingModelNumericRangeInvalid'))
      return false
    }
  }
  return true
}

const loadThingModel = async () => {
  if (!props.productKey || !isSupportedProtocol.value) {
    resetPanel()
    return
  }
  try {
    const data = await getThingModel(props.productKey)
    thingModel.value = {
      version: data.version || 'v1',
      modelJson: data.modelJson || ''
    }
    syncParsedForm()
  } catch (error) {
    const message = error?.response?.data?.message || error?.message || ''
    if (message.includes('thing model not found')) {
      thingModel.value = { version: 'v1', modelJson: '' }
      parsedForm.value = createEmptyThingModelForm()
      return
    }
    ElMessage.error(t('ThingModelLoadFailed'))
  }
}

const startAdd = (section) => {
  activeSection.value = section
  editIndex.value = -1
  draftItem.value = createDraft(section)
  enumItemsExpanded.value = false
  nextTick(() => {
    formRef.value?.clearValidate?.()
  })
}

const startEdit = (section, row, index) => {
  activeSection.value = section
  editIndex.value = index
  draftItem.value = JSON.parse(JSON.stringify(row))
  enumItemsExpanded.value = false
  nextTick(() => {
    formRef.value?.clearValidate?.()
  })
}

const cancelEdit = () => {
  formRef.value?.clearValidate?.()
  activeSection.value = ''
  editIndex.value = -1
  draftItem.value = {}
  enumItemsExpanded.value = false
}

const submitSectionEdit = async () => {
  const section = activeSection.value
  const index = editIndex.value
  const item = JSON.parse(JSON.stringify(draftItem.value))
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (!validateSectionDraft(section, item, index)) {
    return
  }

  const nextState = JSON.parse(JSON.stringify(parsedForm.value))
  if (index > -1) {
    nextState[section][index] = item
  } else {
    nextState[section].push(item)
  }
  await persistThingModel(nextState)
  cancelEdit()
}

const removeItem = async (section, index) => {
  try {
    await ElMessageBox.confirm(
      t('DeleteConfirmLabel'),
      t('NoticeTitle'),
      {
        type: 'warning',
        confirmButtonText: t('ConfirmButtonText'),
        cancelButtonText: t('CancelButtonText')
      }
    )
  } catch {
    return
  }
  const nextState = JSON.parse(JSON.stringify(parsedForm.value))
  nextState[section].splice(index, 1)
  await persistThingModel(nextState)
  if (activeSection.value === section && editIndex.value === index) {
    cancelEdit()
  }
}

watch(
  () => [props.productKey, props.protocolType],
  async () => {
    await loadThingModel()
  },
  { immediate: true }
)
</script>

<style scoped>
.panel-alert {
  margin-bottom: 16px;
}

.panel-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.toolbar-actions :deep(.toolbar-action-btn.el-button) {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
}

.toolbar-actions :deep(.toolbar-action-btn.el-button:hover),
.toolbar-actions :deep(.toolbar-action-btn.el-button:focus-visible) {
  background: var(--el-color-primary-light-3);
  border-color: var(--el-color-primary-light-3);
  color: #fff;
}

.toolbar-actions :deep(.toolbar-action-btn.el-button.is-disabled),
.toolbar-actions :deep(.toolbar-action-btn.el-button.is-disabled:hover) {
  background: var(--el-color-primary-light-7);
  border-color: var(--el-color-primary-light-7);
  color: #fff;
}

.table-section + .table-section {
  margin-top: 20px;
}

.model-section-card {
  padding: 0;
  border: none;
  border-radius: 0;
  background: #fff;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-header-inline {
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.table-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.record-list {
  display: grid;
  gap: 12px;
}

.property-record-list {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.event-record-list {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.service-record-list {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.record-list {
  gap: 12px;
}

.record-card {
  padding: 16px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  min-width: 0;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.record-card:hover {
  border-color: var(--el-border-color);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.record-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin: -16px -16px 12px;
  padding: 12px 16px 10px;
  background: var(--el-fill-color-light);
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.record-title-group {
  min-width: 0;
}

.record-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 22px;
}

.record-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  word-break: break-all;
}

.record-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.record-meta-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-item-full {
  grid-column: 1 / -1;
}

.meta-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.meta-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.meta-value {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
  word-break: break-word;
}

.nested-block-wrap {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.nested-block {
  margin-top: 16px;
}

.nested-block-wrap .nested-block {
  margin-top: 0;
}

.nested-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.nested-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.nested-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.nested-item {
  padding: 12px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-extra-light);
}

.nested-item-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.nested-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.nested-code {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  word-break: break-all;
}

.nested-item-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.nested-enum-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nested-empty {
  padding: 12px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.dialog-form {
  display: flex;
  flex-direction: column;
}

.param-block {
  margin-top: 20px;
}

.param-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.param-footer {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}

.param-card {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.param-card-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.param-form {
  display: flex;
  flex-direction: column;
}

.field-item {
  margin-bottom: 16px;
}

.field-item :deep(.el-form-item__label) {
  padding-bottom: 6px;
  line-height: 20px;
}

.field-item :deep(.el-select) {
  width: 100%;
}

.data-type-group {
  display: flex;
  flex-wrap: wrap;
}

.data-type-group :deep(.el-radio-button) {
  margin: 0;
}

.data-type-group :deep(.el-radio-button__inner) {
  min-width: 84px;
  padding: 10px 18px;
  border-radius: 0;
  box-shadow: none;
}

.data-type-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-top-left-radius: 6px;
  border-bottom-left-radius: 6px;
}

.data-type-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-top-right-radius: 6px;
  border-bottom-right-radius: 6px;
}

.range-input-group {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 44px minmax(0, 1fr);
  align-items: stretch;
}

.range-input-group :deep(.el-input__wrapper) {
  border-radius: 0;
}

.range-input-group :deep(.el-input:first-child .el-input__wrapper) {
  border-top-left-radius: 6px;
  border-bottom-left-radius: 6px;
}

.range-input-group :deep(.el-input:last-child .el-input__wrapper) {
  border-top-right-radius: 6px;
  border-bottom-right-radius: 6px;
}

.range-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--el-border-color);
  border-bottom: 1px solid var(--el-border-color);
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-blank);
}

.enum-form-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.enum-form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.enum-item-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enum-toggle-button {
  margin-top: 8px;
  padding-left: 0;
}

.enum-item-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: 12px;
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
}

.enum-item-value {
  font-weight: 500;
  color: var(--el-text-color-primary);
  word-break: break-word;
}

.enum-item-description {
  color: var(--el-text-color-regular);
  word-break: break-word;
}

.editor-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.json-editor :deep(.el-textarea__inner) {
  font-family: Consolas, "Courier New", monospace;
  line-height: 1.6;
  min-height: 420px !important;
}

@media (max-width: 900px) {
  .record-meta-grid,
  .nested-block-wrap {
    grid-template-columns: 1fr;
  }

  .property-record-list,
  .event-record-list,
  .service-record-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .record-card-header,
  .nested-item-header {
    flex-direction: column;
  }

  .nested-list,
  .enum-form-row,
  .enum-item-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .property-record-list,
  .event-record-list,
  .service-record-list {
    grid-template-columns: 1fr;
  }
}
</style>


const DEFAULT_VERSION = 'v1'

const DATA_TYPE_OPTIONS = [
  { value: 'int', labelKey: 'ThingModelDataTypeInt' },
  { value: 'float', labelKey: 'ThingModelDataTypeFloat' },
  { value: 'enum', labelKey: 'ThingModelDataTypeEnum' },
  { value: 'bool', labelKey: 'ThingModelDataTypeBool' },
  { value: 'time', labelKey: 'ThingModelDataTypeTime' },
  { value: 'string', labelKey: 'ThingModelDataTypeString' }
]

function normalizeDataType(value) {
  if (value === 'double') return 'float'
  if (value === 'text') return 'string'
  return value || 'string'
}

const ACCESS_MODE_OPTIONS = [
  { value: 'r', labelKey: 'ThingModelAccessModeReadOnly' },
  { value: 'rw', labelKey: 'ThingModelAccessModeReadWrite' },
  { value: 'w', labelKey: 'ThingModelAccessModeWriteOnly' }
]

function createParam() {
  return {
    identifier: '',
    name: '',
    dataType: 'string',
    description: '',
    minValue: '',
    maxValue: '',
    step: '',
    unit: '',
    trueText: '',
    falseText: '',
    length: '',
    enumValueType: 'int',
    enumCount: '',
    enumItems: []
  }
}

function createProperty() {
  return {
    identifier: '',
    name: '',
    dataType: 'string',
    accessMode: 'rw',
    description: '',
    minValue: '',
    maxValue: '',
    step: '',
    unit: '',
    trueText: '',
    falseText: '',
    length: '',
    enumValueType: 'int',
    enumCount: '',
    enumItems: []
  }
}

function createEvent() {
  return {
    identifier: '',
    name: '',
    description: '',
    outputParams: [createParam()]
  }
}

function createService() {
  return {
    identifier: '',
    name: '',
    description: '',
    inputParams: [createParam()],
    outputParams: [createParam()]
  }
}

function createEmptyThingModelForm() {
  return {
    version: DEFAULT_VERSION,
    properties: [],
    events: [],
    services: []
  }
}

function normalizeParam(item = {}) {
  return {
    identifier: item.identifier || '',
    name: item.name || '',
    dataType: normalizeDataType(item.dataType),
    description: item.description || '',
    minValue: item.minValue ?? '',
    maxValue: item.maxValue ?? '',
    step: item.step ?? '',
    unit: item.unit || '',
    trueText: item.trueText || '',
    falseText: item.falseText || '',
    length: item.length ?? '',
    enumValueType: normalizeDataType(item.enumValueType || 'int'),
    enumCount: Array.isArray(item.enumItems) ? item.enumItems.length : '',
    enumItems: Array.isArray(item.enumItems)
      ? item.enumItems.map((enumItem) => ({
          value: enumItem.value ?? '',
          description: enumItem.description || ''
        }))
      : []
  }
}

function buildParamPayload(param) {
  return {
    identifier: param.identifier?.trim(),
    name: param.name?.trim() || '',
    dataType: param.dataType,
    description: param.description?.trim() || '',
    ...(param.dataType === 'int' || param.dataType === 'float'
      ? {
          minValue: Number(param.minValue),
          maxValue: Number(param.maxValue),
          step: Number(param.step),
          unit: param.unit?.trim() || ''
        }
      : param.dataType === 'bool'
        ? {
            trueText: param.trueText?.trim() || '',
            falseText: param.falseText?.trim() || ''
          }
        : param.dataType === 'string'
          ? {
              length: Number(param.length)
            }
          : param.dataType === 'enum'
            ? {
                enumValueType: param.enumValueType,
                enumItems: (param.enumItems || []).map((enumItem) => ({
                  value: enumItem.value,
                  description: enumItem.description?.trim() || ''
                }))
              }
            : {})
  }
}

function parseThingModelJson(modelJson = '', version = DEFAULT_VERSION) {
  if (!modelJson) {
    return createEmptyThingModelForm()
  }
  const parsed = JSON.parse(modelJson)
  return {
    version: version || DEFAULT_VERSION,
    properties: Array.isArray(parsed.properties)
      ? parsed.properties.map((item) => ({
        identifier: item.identifier || '',
        name: item.name || '',
        dataType: normalizeDataType(item.dataType),
        accessMode: item.accessMode || 'rw',
        description: item.description || '',
        minValue: item.minValue ?? '',
        maxValue: item.maxValue ?? '',
        step: item.step ?? '',
        unit: item.unit || '',
        trueText: item.trueText || '',
        falseText: item.falseText || '',
        length: item.length ?? '',
        enumValueType: normalizeDataType(item.enumValueType || 'int'),
        enumCount: Array.isArray(item.enumItems) ? item.enumItems.length : '',
        enumItems: Array.isArray(item.enumItems)
          ? item.enumItems.map((enumItem) => ({
              value: enumItem.value ?? '',
              description: enumItem.description || ''
            }))
          : []
      }))
      : [],
    events: Array.isArray(parsed.events)
      ? parsed.events.map((item) => ({
        identifier: item.identifier || '',
        name: item.name || '',
        description: item.description || '',
        outputParams: Array.isArray(item.outputData) ? item.outputData.map(normalizeParam) : []
      }))
      : [],
    services: Array.isArray(parsed.services)
      ? parsed.services.map((item) => ({
        identifier: item.identifier || '',
        name: item.name || '',
        description: item.description || '',
        inputParams: Array.isArray(item.inputData) ? item.inputData.map(normalizeParam) : [],
        outputParams: Array.isArray(item.outputData) ? item.outputData.map(normalizeParam) : []
      }))
      : []
  }
}

function validateThingModelForm(formState) {
  const sections = [
    formState.properties || [],
    formState.events || [],
    formState.services || []
  ]
  for (const section of sections) {
    const ids = new Set()
    for (const item of section) {
      if (!item.identifier?.trim()) {
        return { valid: false, errorKey: 'ThingModelIdentifierRequired' }
      }
      const key = item.identifier.trim()
      if (ids.has(key)) {
        return { valid: false, errorKey: 'ThingModelIdentifierDuplicate' }
      }
      ids.add(key)
    }
  }
  return { valid: true }
}

function buildThingModelJson(formState) {
  return JSON.stringify({
    properties: (formState.properties || []).map((item) => ({
      identifier: item.identifier?.trim(),
      name: item.name?.trim() || '',
      dataType: item.dataType,
      accessMode: item.accessMode,
      description: item.description?.trim() || '',
      ...(item.dataType === 'int' || item.dataType === 'float'
        ? {
            minValue: Number(item.minValue),
            maxValue: Number(item.maxValue),
            step: Number(item.step),
            unit: item.unit?.trim() || ''
          }
        : item.dataType === 'bool'
          ? {
              trueText: item.trueText?.trim() || '',
              falseText: item.falseText?.trim() || ''
            }
          : item.dataType === 'string'
            ? {
                length: Number(item.length)
              }
            : item.dataType === 'enum'
              ? {
                  enumValueType: item.enumValueType,
                  enumItems: (item.enumItems || []).map((enumItem) => ({
                    value: enumItem.value,
                    description: enumItem.description?.trim() || ''
                  }))
                }
              : {})
    })),
    events: (formState.events || []).map((item) => ({
      identifier: item.identifier?.trim(),
      name: item.name?.trim() || '',
      description: item.description?.trim() || '',
      outputData: (item.outputParams || [])
        .filter((param) => param.identifier?.trim())
        .map(buildParamPayload)
    })),
    services: (formState.services || []).map((item) => ({
      identifier: item.identifier?.trim(),
      name: item.name?.trim() || '',
      description: item.description?.trim() || '',
      inputData: (item.inputParams || [])
        .filter((param) => param.identifier?.trim())
        .map(buildParamPayload),
      outputData: (item.outputParams || [])
        .filter((param) => param.identifier?.trim())
        .map(buildParamPayload)
    }))
  }, null, 2)
}

export {
  DEFAULT_VERSION,
  DATA_TYPE_OPTIONS,
  ACCESS_MODE_OPTIONS,
  createParam,
  createProperty,
  createEvent,
  createService,
  createEmptyThingModelForm,
  parseThingModelJson,
  buildThingModelJson,
  validateThingModelForm
}


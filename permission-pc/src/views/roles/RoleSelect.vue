<template>
  <el-select v-model="innerValue" :placeholder="placeholder" clearable filterable multiple @change="handleChange">
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value"
      :disabled="item.disabled"
    />
  </el-select>
</template>

<script setup name="RoleSelect">
import { ref, onMounted, watch } from 'vue'
import { getRoleList } from '@/views/roles/RoleApi'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: 'Please select role'
  },
  // IDs that should be disabled (string or number)
  disabledIds: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// Initialize innerValue - ensure it's always an array
const getInitialValue = () => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.length > 0 ? [...props.modelValue] : []
  }
  if (props.modelValue !== null && props.modelValue !== undefined && props.modelValue !== '') {
    return [props.modelValue]
  }
  return []
}
const innerValue = ref(getInitialValue())
const options = ref([])

const handleChange = (value) => {
  // Filter out invalid values (null, undefined, empty string)
  const validValue = Array.isArray(value) 
    ? value.filter(v => v !== null && v !== undefined && v !== '')
    : []
  innerValue.value = validValue
  emit('update:modelValue', validValue)
  emit('change', validValue)
}

// Format role list to options
const formatOptions = (roles) => {
  const disabledIdSet = new Set(
    Array.isArray(props.disabledIds)
      ? props.disabledIds.map(id => String(id))
      : []
  )

  return Array.isArray(roles)
    ? roles.map(role => ({
        label: role.name || role.label,
        value: role.id,
        disabled: disabledIdSet.has(String(role.id))
      }))
    : []
}

onMounted(() => {
  getRoleList().then(res => {
    // Handle response format: could be array directly or wrapped in data property
    const roleList = Array.isArray(res) ? res : (res.data || res.list || res.records || [])
    options.value = formatOptions(roleList)

    // Get valid option values for validation
    const validOptionValues = new Set(options.value.map(opt => opt.value))

    // Sync with props.modelValue after options are loaded
    let newValue = []
    if (Array.isArray(props.modelValue) && props.modelValue.length > 0) {
      // Filter out invalid values and values that don't exist in options
      newValue = props.modelValue.filter(v => 
        v !== null && 
        v !== undefined && 
        v !== '' && 
        validOptionValues.has(v)
      )
    } else if (props.modelValue !== null && props.modelValue !== undefined && props.modelValue !== '') {
      // Handle single value (backward compatibility)
      const singleValue = props.modelValue
      if (validOptionValues.has(singleValue)) {
        newValue = [singleValue]
      }
    }
    
    innerValue.value = newValue
    // Emit update if value was filtered
    if (JSON.stringify(newValue.sort()) !== JSON.stringify((Array.isArray(props.modelValue) ? props.modelValue : []).sort())) {
      emit('update:modelValue', newValue)
    }
  }).catch(error => {
    console.error('Failed to load role list:', error)
  })
})

// Watch for external changes to modelValue
watch(() => props.modelValue, (newVal) => {
  // Get valid option values for validation
  const validOptionValues = new Set(options.value.map(opt => opt.value))
  
  let newValue = []
  if (Array.isArray(newVal)) {
    // Filter out invalid values and values that don't exist in options
    newValue = newVal.filter(v => 
      v !== null && 
      v !== undefined && 
      v !== '' && 
      validOptionValues.has(v)
    )
  } else if (newVal !== null && newVal !== undefined && newVal !== '') {
    // Handle single value (backward compatibility)
    if (validOptionValues.has(newVal)) {
      newValue = [newVal]
    }
  }
  
  // Deep comparison to avoid unnecessary updates
  const currentSorted = [...innerValue.value].sort()
  const newSorted = [...newValue].sort()
  if (JSON.stringify(currentSorted) !== JSON.stringify(newSorted)) {
    innerValue.value = newValue
  }
}, { deep: true })

// When disabledIds changes, re-apply disabled state to options
watch(
  () => props.disabledIds,
  () => {
    getRoleList().then(res => {
      const roleList = Array.isArray(res) ? res : (res.data || res.list || res.records || [])
      options.value = formatOptions(roleList)
    })
  },
  { deep: true }
)
</script>

<style scoped></style>


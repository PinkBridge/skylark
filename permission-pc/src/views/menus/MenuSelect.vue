<template>
  <el-tree-select v-model="innerValue" :data="data" :render-after-expand="false" default-expand-all clearable
  @update:model-value="handleChange" check-strictly="true" :placeholder="placeholder"/>
</template>

<script setup name="MenuSelect">
import { ref, onMounted, watch } from 'vue'
import { getMenuList } from '@/views/menus/MenuApi'

const props = defineProps({
  modelValue: { 
    type: [String, Number],
    default: null
  },
  placeholder: {
    type: String,
    default: 'Please select menu'
  },
  // IDs that should be disabled (string or number)
  disabledIds: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const innerValue = ref(props.modelValue)
const data = ref([])
// keep raw tree from backend so we can re-apply disabled state when props change
const rawTree = ref([])

const handleChange = (value) => {
  innerValue.value = value
  emit('update:modelValue', value)
  emit('change', value)
}

const formatTree = (tree) => {
  const disabledIdSet = new Set(
    Array.isArray(props.disabledIds)
      ? props.disabledIds.map(id => String(id))
      : []
  )

  return Array.isArray(tree)
    ? tree.map(node => {
        const formatted = {
          ...node,
          label: node.name || node.label,
          value: node.id,
          // normalize both sides to string to avoid 1 vs '1' comparison issues
          disabled: disabledIdSet.has(String(node.id))
        }
        if (node.children && node.children.length) {
          formatted.children = formatTree(node.children)
        }
        return formatted
      })
    : []
}

onMounted(() => {
  getMenuList().then(res => {
    // Handle response format: could be array directly or wrapped in data property
    const menuTree = Array.isArray(res) ? res : (res.data || res.list || [])
    rawTree.value = menuTree
    const formattedData = formatTree(rawTree.value)
    data.value = formattedData
    
    // If parent component has passed a value, use it; otherwise select the first one
    if (props.modelValue !== null && props.modelValue !== undefined && props.modelValue !== '') {
      // Parent component has passed a value, use it
      innerValue.value = props.modelValue
    }
  }).catch(error => {
    console.error('Failed to load menu list:', error)
  })
})

// Watch for external changes to modelValue
watch(() => props.modelValue, (newVal) => {
  if (newVal !== innerValue.value) {
    innerValue.value = newVal
  }
})

// When disabledIds changes, re-apply disabled state to tree nodes
watch(
  () => props.disabledIds,
  () => {
    data.value = formatTree(rawTree.value)
  },
  { deep: true }
)
</script>

<style scoped></style>

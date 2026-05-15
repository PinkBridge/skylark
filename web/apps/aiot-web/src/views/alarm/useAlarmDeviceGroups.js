import { computed, ref } from 'vue'
import { listDeviceGroups } from '@/views/device-groups/DeviceGroupApi'

export function useAlarmDeviceGroups() {
  const deviceGroupOptions = ref([])

  async function loadDeviceGroups() {
    try {
      const raw = await listDeviceGroups({ pageNum: 1, pageSize: 1000 })
      deviceGroupOptions.value = Array.isArray(raw?.records) ? raw.records : (Array.isArray(raw) ? raw : [])
    } catch {
      deviceGroupOptions.value = []
    }
  }

  const deviceGroupNameByKey = computed(() => {
    const map = {}
    for (const g of deviceGroupOptions.value || []) {
      if (g && g.groupKey != null && g.groupKey !== '') {
        const key = String(g.groupKey)
        const n = g.name != null ? String(g.name).trim() : ''
        map[key] = n
      }
    }
    return map
  })

  function deviceGroupCellLabel(groupKey) {
    if (groupKey == null || groupKey === '') return '—'
    const k = String(groupKey)
    const name = deviceGroupNameByKey.value[k]
    if (name) return name
    return '—'
  }

  return { deviceGroupOptions, loadDeviceGroups, deviceGroupCellLabel }
}

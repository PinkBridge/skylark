<template>
  <el-dialog :model-value="visible" @update:model-value="(v) => { if (!v) onCancel() }" @close="onCancel" :title="t('SelectTitle')" align-center destroy-on-close :show-close="false"
    :modal="false" modal-penetrable width="80%">
    <div
      v-loading="!oauthClientsReady"
      :element-loading-text="t('AppsListLoading')"
      class="menu-select-search-wrap"
    >
      <MenuSearchForm
        v-if="oauthClientsReady"
        :client-ids="oauthClientIds"
        :app="selectedApp"
        @update:app="onAppFilterChange"
        :search="handleSearch"
        :reset="handleReset"
      />
    </div>
    <el-table :data="tableData" style="width: 100%" stripe border show-overflow-tooltip selection-mode="multiple"
      ref="menuTableRef" row-key="id" @selection-change="handleSelectionChange" @select="handleSelect"
      :tree-props="{ checkStrictly: true }">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" :label="t('IDLabel')" width="80" />
      <el-table-column prop="name" :label="t('NameLabel')" width="150" />
      <el-table-column prop="icon" :label="t('IconLabel')" width="80">
        <template #default="{ row }">
          <el-icon v-if="row.icon" :size="20">
            <component :is="row.icon" />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="type" :label="t('TypeLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type == 'menu' ? 'success' : 'danger'">
            {{ row.type == 'menu' ? t('MenuLabel') : t('ButtonLabel') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" :label="t('PathLabel')" min-width="150" />
      <el-table-column prop="permlabel" :label="t('PermLabel')" min-width="150" />
      <el-table-column prop="moduleKey" :label="t('ModuleKeyLabel')" min-width="120" />
      <el-table-column prop="hidden" :label="t('HiddenLabel')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.hidden == true ? 'danger' : 'success'">
            {{ row.hidden == true ? t('Yes') : t('No') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="t('CreatedAtLabel')" width="180" />
      <el-table-column prop="updateTime" :label="t('UpdatedAtLabel')" width="180" />
    </el-table>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="confirmLoading" @click="handleConfirm">{{ t('ConfirmButtonText') }}</el-button>
        <el-button type="default" @click="onCancel">{{ t('CancelButtonText') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="MenuSelectTableDialog">
import { ref, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { getGrantableMenuTree } from '@/views/menus/MenuApi'
import { getAppList } from '@/views/apps/AppApi'
import MenuSearchForm from '@/views/menus/MenuSearchForm.vue'
import { getRoleById, toggleRoleMenu } from '@/views/roles/RoleApi'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = defineProps(['visible', 'row', 'onConfirm', 'onCancel'])

// data
const tableData = ref([])
const multipleSelection = ref([])
const menuTableRef = ref(null)
const searchParams = ref({})
const oauthClientIds = ref([])
/** 应用列表已拉取后再挂载筛选表单，避免首次打开时 el-select 在空 options 下不刷新 */
const oauthClientsReady = ref(false)
const selectedApp = ref('')

const onAppFilterChange = (v) => {
  selectedApp.value = v || ''
}

const normalizeMenuTreeResponse = (response) =>
  Array.isArray(response) ? response : (response.data || response.list || [])
/** 打开弹窗时服务端已绑定的菜单 ID，用于确认时与当前勾选做对称差后一次性 toggle */
const baselineMenuIds = ref([])
const confirmLoading = ref(false)
/** 避免勾选父级联动子级时触发的 select 递归 */
const isSyncingSelection = ref(false)

// Expand first level only
const expandFirstLevel = () => {
  const table = menuTableRef.value
  if (!table || !Array.isArray(tableData.value)) return

  nextTick(() => {
    tableData.value.forEach(rootNode => {
      table.toggleRowExpansion(rootNode, true)
    })
  })
}

async function loadOAuthClients() {
  try {
    const list = await getAppList()
    const rows = Array.isArray(list) ? list : []
    oauthClientIds.value = rows.map((c) => c.clientId).filter(Boolean).sort()
    if (!selectedApp.value || !oauthClientIds.value.includes(selectedApp.value)) {
      selectedApp.value = oauthClientIds.value[0] || ''
    }
  } catch (e) {
    console.error('Failed to load OAuth clients:', e)
  } finally {
    oauthClientsReady.value = true
  }
}

const grantableQuery = () => {
  const app = searchParams.value.app ?? selectedApp.value ?? oauthClientIds.value[0] ?? ''
  return app ? { app } : {}
}

const fetchGrantableMenusAndRole = () => {
  getGrantableMenuTree(grantableQuery()).then(response => {
    tableData.value = normalizeMenuTreeResponse(response)
    expandFirstLevel()

    getRoleById(props.row.id).then(response => {
      const menuIds = response.menuIds || []
      baselineMenuIds.value = [...menuIds]
      nextTick(() => {
        setDefaultSelection(menuIds)
      })
    })
  }).catch(error => {
    console.error('Failed to get menu list:', error)
  })
}

// init data
const initData = async () => {
  oauthClientsReady.value = false
  tableData.value = []
  await loadOAuthClients()
  const app = selectedApp.value || oauthClientIds.value[0] || ''
  selectedApp.value = app
  searchParams.value = { app }

  fetchGrantableMenusAndRole()
}

// reset data
const handleReset = (params) => {
  searchParams.value = params || {}
  selectedApp.value = params?.app || ''
  fetchGrantableMenusAndRole()
}

// filter data
const handleSearch = (params) => {
  searchParams.value = params
  if (params.app !== undefined && params.app !== null) {
    selectedApp.value = params.app
  }
  fetchGrantableMenusAndRole()
}

// cancel
const onCancel = () => {
  props.onCancel()
}

const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

/** 扁平收集节点下全部子孙（不含自身） */
function collectDescendantNodes(node) {
  if (!node?.children?.length) {
    return []
  }
  const out = []
  for (const c of node.children) {
    out.push(c, ...collectDescendantNodes(c))
  }
  return out
}

const handleSelect = (selection, row) => {
  if (isSyncingSelection.value) {
    return
  }
  const table = menuTableRef.value
  if (!table) {
    return
  }
  const selected = selection.some((r) => r.id === row.id)
  const descendants = collectDescendantNodes(row)
  if (descendants.length === 0) {
    return
  }
  isSyncingSelection.value = true
  nextTick(() => {
    try {
      for (const n of descendants) {
        table.toggleRowSelection(n, selected)
      }
    } finally {
      nextTick(() => {
        isSyncingSelection.value = false
      })
    }
  })
}

/** 与 baseline 相比需要翻转绑定状态的菜单 id（对称差） */
function idsToToggle(baseline, selectedSet) {
  const base = new Set(baseline.map((id) => Number(id)))
  const out = []
  for (const id of base) {
    if (!selectedSet.has(id)) {
      out.push(id)
    }
  }
  for (const id of selectedSet) {
    if (!base.has(id)) {
      out.push(id)
    }
  }
  return out
}

/** 当前表格树内的全部菜单 id（当前应用/筛选范围），用于限定差分不影响其它应用的绑定 */
function collectMenuIdsFromTree(nodes) {
  if (!Array.isArray(nodes) || nodes.length === 0) {
    return []
  }
  const out = []
  for (const n of nodes) {
    if (n?.id != null) {
      out.push(Number(n.id))
    }
    if (Array.isArray(n.children) && n.children.length > 0) {
      out.push(...collectMenuIdsFromTree(n.children))
    }
  }
  return out
}

const handleConfirm = () => {
  const table = menuTableRef.value
  if (!table || !props.row?.id) {
    return
  }
  const rows = table.getSelectionRows()
  const selectedSet = new Set(rows.map((r) => Number(r.id)))
  const visibleIds = new Set(collectMenuIdsFromTree(tableData.value))
  const baselineScoped = baselineMenuIds.value.filter((id) => visibleIds.has(Number(id)))
  const toToggle = idsToToggle(baselineScoped, selectedSet)
  if (toToggle.length === 0) {
    props.onConfirm()
    return
  }
  confirmLoading.value = true
  toggleRoleMenu(props.row.id, toToggle)
    .then(() => getRoleById(props.row.id))
    .then((roleDto) => {
      ElMessage.success(t('UpdateSuccess'))
      baselineMenuIds.value = [...(roleDto.menuIds || [])]
      props.onConfirm()
    })
    .catch((error) => {
      ElMessage.error(error.message || 'Save failed')
    })
    .finally(() => {
      confirmLoading.value = false
    })
}

const setDefaultSelection = (menuIds) => {
  const table = menuTableRef.value
  if (!table || !Array.isArray(tableData.value) || !Array.isArray(menuIds)) {
    return
  }

  // Recursive function to find and select nodes
  const selectNodes = (nodes) => {
    nodes.forEach(node => {
      // Clear selection first
      table.toggleRowSelection(node, false)
      
      // Check if this node should be selected
      if (menuIds.includes(node.id)) {
        nextTick(() => {
          table.toggleRowSelection(node, true)
        })
      }
      
      // Recursively process children
      if (node.children && Array.isArray(node.children) && node.children.length > 0) {
        selectNodes(node.children)
      }
    })
  }

  // First clear all selections
  tableData.value.forEach(row => {
    table.toggleRowSelection(row, false)
  })

  // Then select matching nodes
  nextTick(() => {
    selectNodes(tableData.value)
  })
}

// 父组件先设 row 再设 visible=true 时，挂载瞬间 visible 已是 true，默认 watch 不会触发，需 immediate
watch(
  () => props.visible,
  (newVal) => {
    if (newVal && props.row?.id) {
      initData()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.buttons-block {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

.menu-select-search-wrap {
  min-height: 48px;
  margin-bottom: 12px;
}
</style>


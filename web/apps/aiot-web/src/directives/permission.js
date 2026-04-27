import { createPermissionDirective } from '@skylark/authz-vue'
import { hasPermission } from '@/utils/permission'

export default createPermissionDirective(hasPermission)

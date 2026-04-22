export { ADMIN_SHELL_CONFIG } from './symbols'
export { shellMessages } from './i18n/messages'
export {
  menuPermLabelMap,
  setMenuTreeForPermLabels,
  collectPermissionLabelsFromTree,
  persistPermissionLabelsFromMenuTree
} from './utils/menuPermission'

export { resolvePostLoginRouteName } from '@skylark/login-redirect-client'

export { default as ShellWelcome } from './components/ShellWelcome.vue'
export { default as ShellHomeLayout } from './components/ShellHomeLayout.vue'
export { default as ShellHeader } from './components/ShellHeader.vue'
export { default as ShellSidebarMenu } from './components/ShellSidebarMenu.vue'
export { default as ShellLanguageSelect } from './components/ShellLanguageSelect.vue'
export { default as ShellUserAvatar } from './components/ShellUserAvatar.vue'
export { default as ShellUserInfo } from './components/ShellUserInfo.vue'

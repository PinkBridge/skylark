-- 3) sys_menu：页面菜单保持业务语义；按钮文案统一为「新建 / 详情 / 编辑 / 删除」等短语义（英文对应 New / Detail / Edit / Delete）

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `sys_role_menu`;
TRUNCATE TABLE `sys_menu`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_menu`(`id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`) VALUES
  (1000, NULL, '权限管理', JSON_OBJECT('zh', '权限管理', 'en', 'Permission'), '/perm', 'Lock', 10, 0, 'menu', 'perm.group', 'permission'),
  (1001, 1000, '租户管理', JSON_OBJECT('zh', '租户管理', 'en', 'Tenants'), '/perm/tenants', 'DataAnalysis', 11, 0, 'menu', 'perm.tenants.view', 'permission.tenant'),
  (1002, 1000, '用户管理', JSON_OBJECT('zh', '用户管理', 'en', 'Users'), '/perm/users', 'User', 12, 0, 'menu', 'perm.users.view', 'permission.user'),
  (1003, 1000, '组织管理', JSON_OBJECT('zh', '组织管理', 'en', 'Organizations'), '/perm/orgs', 'OfficeBuilding', 13, 0, 'menu', 'perm.orgs.view', 'permission.org'),
  (1004, 1000, '角色管理', JSON_OBJECT('zh', '角色管理', 'en', 'Roles'), '/perm/roles', 'UserFilled', 14, 0, 'menu', 'perm.roles.view', 'permission.role'),
  (1005, NULL, '系统管理', JSON_OBJECT('zh', '系统管理', 'en', 'System'), '/system', 'Setting', 20, 0, 'menu', 'system.group', 'system'),
  (1006, 1005, '菜单管理', JSON_OBJECT('zh', '菜单管理', 'en', 'Menus'), '/system/menus', 'Menu', 21, 0, 'menu', 'system.menus.view', 'system.menu'),
  (1007, 1005, '接口管理', JSON_OBJECT('zh', '接口管理', 'en', 'APIs'), '/system/apis', 'Link', 22, 0, 'menu', 'system.apis.view', 'system.api'),
  (1008, 1005, '应用管理', JSON_OBJECT('zh', '应用管理', 'en', 'Applications'), '/system/apps', 'Monitor', 23, 0, 'menu', 'system.apps.view', 'system.app'),
  (1009, 1005, '资源管理', JSON_OBJECT('zh', '资源管理', 'en', 'Resources'), '/perm/resources', 'Files', 24, 0, 'menu', 'perm.resources.view', 'system.resource'),
  (1010, NULL, '系统记录', JSON_OBJECT('zh', '系统记录', 'en', 'Logs'), '/logger', 'Document', 30, 0, 'menu', 'logger.group', 'logger'),
  (1011, 1010, '登录日志', JSON_OBJECT('zh', '登录日志', 'en', 'Login logs'), '/logger/loging', 'List', 31, 0, 'menu', 'logger.login.view', 'logger.login'),
  (1012, 1005, '白名单管理', JSON_OBJECT('zh', '白名单管理', 'en', 'Whitelist'), '/system/whitelist', 'Finished', 25, 0, 'menu', 'system.whitelist.view', 'system.whitelist'),
  (1013, 1005, '租户信息', JSON_OBJECT('zh', '租户信息', 'en', 'Tenant profile'), '/system/tenant-profile', 'OfficeBuilding', 26, 0, 'menu', 'system.tenant.profile.view', 'system.tenant.profile'),
  (1014, 1010, '操作记录', JSON_OBJECT('zh', '操作记录', 'en', 'Operation logs'), '/logger/operation-logs', 'Document', 32, 0, 'menu', 'logger.operation.view', 'logger.operation'),
  (1015, 1005, '系统设置', JSON_OBJECT('zh', '系统设置', 'en', 'System setting'), '/system/platform-config', 'Setting', 27, 0, 'menu', 'system.platform.view', 'system.platform'),
  (1016, 1005, '数据域', JSON_OBJECT('zh', '数据域', 'en', 'Data domains'), '/system/data-domains', 'Collection', 28, 0, 'menu', 'system.data-domains.view', 'system.data-domain'),

  (3101, 1001, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'perm.tenants.new', 'permission.tenant'),
  (3102, 1001, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'perm.tenants.detail', 'permission.tenant'),
  (3103, 1001, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'perm.tenants.edit', 'permission.tenant'),
  (3104, 1001, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'perm.tenants.delete', 'permission.tenant'),

  (3201, 1002, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'perm.users.new', 'permission.user'),
  (3202, 1002, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'perm.users.detail', 'permission.user'),
  (3203, 1002, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'perm.users.edit', 'permission.user'),
  (3204, 1002, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'perm.users.delete', 'permission.user'),

  (3301, 1003, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'perm.orgs.new', 'permission.org'),
  (3302, 1003, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'perm.orgs.detail', 'permission.org'),
  (3303, 1003, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'perm.orgs.edit', 'permission.org'),
  (3304, 1003, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'perm.orgs.delete', 'permission.org'),

  (3401, 1004, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'perm.roles.new', 'permission.role'),
  (3402, 1004, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'perm.roles.detail', 'permission.role'),
  (3403, 1004, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'perm.roles.edit', 'permission.role'),
  (3404, 1004, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'perm.roles.delete', 'permission.role'),
  (3405, 1004, '接口授权', JSON_OBJECT('zh', '接口授权', 'en', 'API assignment'), '', NULL, 5, 1, 'button', 'perm.roles.api', 'permission.role'),

  (3601, 1006, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'system.menus.new', 'system.menu'),
  (3602, 1006, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'system.menus.detail', 'system.menu'),
  (3603, 1006, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'system.menus.edit', 'system.menu'),
  (3604, 1006, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'system.menus.delete', 'system.menu'),

  (3701, 1007, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'system.apis.new', 'system.api'),
  (3702, 1007, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'system.apis.detail', 'system.api'),
  (3703, 1007, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'system.apis.edit', 'system.api'),
  (3704, 1007, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'system.apis.delete', 'system.api'),

  (3801, 1008, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'system.apps.new', 'system.app'),
  (3802, 1008, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'system.apps.detail', 'system.app'),
  (3803, 1008, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'system.apps.edit', 'system.app'),
  (3804, 1008, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'system.apps.delete', 'system.app'),

  (3901, 1009, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'perm.resources.new', 'system.resource'),
  (3902, 1009, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'perm.resources.detail', 'system.resource'),
  (3903, 1009, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'perm.resources.edit', 'system.resource'),
  (3904, 1009, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'perm.resources.delete', 'system.resource'),

  (3911, 1011, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 1, 1, 'button', 'logger.login.detail', 'logger.login'),
  (3912, 1011, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 2, 1, 'button', 'logger.login.delete', 'logger.login'),

  (3917, 1012, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'system.whitelist.new', 'system.whitelist'),
  (3918, 1012, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'system.whitelist.detail', 'system.whitelist'),
  (3919, 1012, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'system.whitelist.edit', 'system.whitelist'),
  (3920, 1012, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'system.whitelist.delete', 'system.whitelist'),

  (3921, 1013, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 1, 1, 'button', 'system.tenant.profile.edit', 'system.tenant.profile'),

  (3922, 1014, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 1, 1, 'button', 'logger.operation.detail', 'logger.operation'),

  (3923, 1015, '保存', JSON_OBJECT('zh', '保存', 'en', 'Save'), '', NULL, 1, 1, 'button', 'system.platform.edit', 'system.platform'),

  (3924, 1016, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button', 'system.data-domains.new', 'system.data-domain'),
  (3925, 1016, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button', 'system.data-domains.detail', 'system.data-domain'),
  (3926, 1016, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button', 'system.data-domains.edit', 'system.data-domain'),
  (3927, 1016, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button', 'system.data-domains.delete', 'system.data-domain');

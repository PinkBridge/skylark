-- 2) sys_api：与当前后端 RBAC 路径、前端 permlabel 对齐的整合清单

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `sys_role_api`;
TRUNCATE TABLE `sys_role_menu`;
TRUNCATE TABLE `sys_api`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`) VALUES
  (10001, 'GET',    '/api/permission/users/**',       'perm.users.detail',      'permission.user'),
  (10002, 'POST',   '/api/permission/users/**',       'perm.users.new',         'permission.user'),
  (10003, 'PUT',    '/api/permission/users/**',       'perm.users.edit',        'permission.user'),
  (10004, 'DELETE', '/api/permission/users/**',       'perm.users.delete',      'permission.user'),

  (10005, 'GET',    '/api/permission/roles/**',       'perm.roles.detail',      'permission.role'),
  (10006, 'POST',   '/api/permission/roles/**',       'perm.roles.new',         'permission.role'),
  (10007, 'PUT',    '/api/permission/roles/**',       'perm.roles.edit',        'permission.role'),
  (10008, 'DELETE', '/api/permission/roles/**',       'perm.roles.delete',      'permission.role'),

  (10009, 'GET',    '/api/permission/menus/**',       'system.menus.detail',    'system.menu'),
  (10010, 'POST',   '/api/permission/menus/**',       'system.menus.new',       'system.menu'),
  (10011, 'PUT',    '/api/permission/menus/**',       'system.menus.edit',      'system.menu'),
  (10012, 'DELETE', '/api/permission/menus/**',       'system.menus.delete',    'system.menu'),

  (10013, 'GET',    '/api/permission/apis/**',        'system.apis.detail',     'system.api'),
  (10014, 'POST',   '/api/permission/apis/**',        'system.apis.new',        'system.api'),
  (10015, 'PUT',    '/api/permission/apis/**',        'system.apis.edit',       'system.api'),
  (10016, 'DELETE', '/api/permission/apis/**',        'system.apis.delete',     'system.api'),

  (10021, 'GET',    '/api/permission/apps/**',        'system.apps.detail',     'system.app'),
  (10022, 'POST',   '/api/permission/apps/**',        'system.apps.new',        'system.app'),
  (10023, 'PUT',    '/api/permission/apps/**',        'system.apps.edit',       'system.app'),
  (10024, 'DELETE', '/api/permission/apps/**',        'system.apps.delete',     'system.app'),

  (10025, 'GET',    '/api/permission/resources/**',   'perm.resources.detail',  'system.resource'),
  (10026, 'POST',   '/api/permission/resources/**',   'perm.resources.new',     'system.resource'),
  (10027, 'PUT',    '/api/permission/resources/**',   'perm.resources.edit',    'system.resource'),
  (10028, 'DELETE', '/api/permission/resources/**',   'perm.resources.delete',  'system.resource'),

  (10029, 'GET',    '/api/permission/tenants/**',     'perm.tenants.detail',    'permission.tenant'),
  (10030, 'POST',   '/api/permission/tenants/**',     'perm.tenants.new',       'permission.tenant'),
  (10031, 'PUT',    '/api/permission/tenants/**',     'perm.tenants.edit',      'permission.tenant'),
  (10032, 'DELETE', '/api/permission/tenants/**',     'perm.tenants.delete',    'permission.tenant'),

  (10033, 'GET',    '/api/permission/orgs/**',        'perm.orgs.detail',       'permission.org'),
  (10034, 'POST',   '/api/permission/orgs/**',        'perm.orgs.new',          'permission.org'),
  (10035, 'PUT',    '/api/permission/orgs/**',        'perm.orgs.edit',         'permission.org'),
  (10036, 'DELETE', '/api/permission/orgs/**',        'perm.orgs.delete',       'permission.org'),

  (10037, 'POST',   '/api/permission/roles/*/apis/*:toggle', 'perm.roles.api',  'permission.role'),
  (10038, 'POST',   '/api/permission/roles/*/menus:toggle',   'perm.roles.api',  'permission.role'),
  (10043, 'POST',   '/api/permission/roles/*/menus:bind',     'perm.roles.api',  'permission.role'),

  (10039, 'GET',    '/api/permission/login-logs/**',  'logger.login.detail',    'logger.login'),
  (10040, 'DELETE', '/api/permission/login-logs/**',  'logger.login.delete',    'logger.login'),

  (10041, 'POST',   '/api/permission/tenants/*/admin', 'perm.tenants.edit',     'permission.tenant'),

  (10046, 'GET',    '/api/permission/whitelist/**',   'system.whitelist.detail', 'system.whitelist'),
  (10047, 'POST',   '/api/permission/whitelist/**',   'system.whitelist.new',    'system.whitelist'),
  (10048, 'PUT',    '/api/permission/whitelist/**',   'system.whitelist.edit',   'system.whitelist'),
  (10049, 'DELETE', '/api/permission/whitelist/**',   'system.whitelist.delete', 'system.whitelist'),

  (10050, 'GET',    '/api/permission/tenants/me',     'system.tenant.profile.detail', 'system.tenant.profile'),
  (10051, 'PUT',    '/api/permission/tenants/me',     'system.tenant.profile.edit', 'system.tenant.profile'),

  (10052, 'GET',    '/api/permission/operation-logs/**', 'logger.operation.detail', 'logger.operation'),

  (10053, 'GET',    '/api/permission/platform-configs',     'system.platform.view', 'system.platform'),
  (10055, 'PUT',    '/api/permission/platform-configs/**', 'system.platform.edit', 'system.platform'),

  (10056, 'POST',   '/api/permission/tenants/permissions:reconcile', 'system.platform.edit', 'system.platform'),

  (10057, 'GET',    '/api/permission/data-domains/**',  'system.data-domains.detail', 'system.data-domain'),
  (10058, 'POST',   '/api/permission/data-domains/**',  'system.data-domains.new',    'system.data-domain'),
  (10059, 'PUT',    '/api/permission/data-domains/**',  'system.data-domains.edit',   'system.data-domain'),
  (10060, 'DELETE', '/api/permission/data-domains/**',  'system.data-domains.delete', 'system.data-domain');

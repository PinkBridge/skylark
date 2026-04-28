-- permission-pc: add import buttons for Menu/API management; add backend import endpoints to sys_api; bind to ROLE_SUPER_ADMIN (id=1)

-- 1) sys_api (permission-service import endpoints)
INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`, `is_delete`, `app_code`)
SELECT 10111, 'POST', '/api/permission/menus:import', 'system.menus.import', 'system.menu', 0, 'permission-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_api` WHERE `id` = 10111);

INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`, `is_delete`, `app_code`)
SELECT 10112, 'POST', '/api/permission/apis:import', 'system.apis.import', 'system.api', 0, 'permission-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_api` WHERE `id` = 10112);

-- Bind to super admin role
INSERT INTO `sys_role_api`(`role_id`, `api_id`, `is_delete`)
SELECT 1, 10111, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_api` WHERE `role_id` = 1 AND `api_id` = 10111);

INSERT INTO `sys_role_api`(`role_id`, `api_id`, `is_delete`)
SELECT 1, 10112, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_api` WHERE `role_id` = 1 AND `api_id` = 10112);

-- 2) sys_menu (button nodes for v-permission)
INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  10211, 1006,
  '导入', JSON_OBJECT('zh', '导入', 'en', 'Import'),
  NULL, 'Upload',
  90, 0, 'button',
  'system.menus.import', 'system.menu', 0, 'permission-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 10211);

INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  10212, 1007,
  '导入', JSON_OBJECT('zh', '导入', 'en', 'Import'),
  NULL, 'Upload',
  90, 0, 'button',
  'system.apis.import', 'system.api', 0, 'permission-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 10212);

-- Bind to super admin role
INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 10211, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 10211);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 10212, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 10212);


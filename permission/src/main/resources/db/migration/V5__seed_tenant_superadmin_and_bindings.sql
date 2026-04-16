-- 4) 默认租户、组织、超级管理员角色与用户；sys_tenant_admin_binding；超级管理员拥有全部菜单与 API

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `sys_tenant_admin_binding`;
TRUNCATE TABLE `sys_role_data_domain`;
TRUNCATE TABLE `sys_user_role`;
TRUNCATE TABLE `sys_permission_audit`;
TRUNCATE TABLE `sys_operation_log`;
TRUNCATE TABLE `sys_login_log`;
TRUNCATE TABLE `sys_resource`;
TRUNCATE TABLE `sys_data_domain`;
TRUNCATE TABLE `sys_user`;
TRUNCATE TABLE `sys_role`;
TRUNCATE TABLE `sys_organization`;
TRUNCATE TABLE `sys_tenant`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_tenant`(
  `id`, `name`, `system_name`, `code`, `contact_name`, `contact_phone`, `contact_email`,
  `domain`, `logo`, `address`, `description`, `status`, `expire_time`
) VALUES (
  1, '默认租户', 'Skylark 平台', 'DEFAULT',
  'System Admin', '13800000000', 'admin@skylark.local',
  'localhost', NULL, 'localhost', '系统默认租户', 'ACTIVE', '2099-12-31 23:59:59'
);

INSERT INTO `sys_organization`(
  `id`, `name`, `code`, `parent_id`, `type`, `level`, `sort`, `leader`, `phone`, `email`,
  `address`, `description`, `status`, `tenant_id`
) VALUES (
  1, '默认组织', 'DEFAULT_ORG', NULL, 'COMPANY', 1, 1, 'System Admin', '13800000000',
  'admin@skylark.local', 'localhost', '系统默认组织', 'ACTIVE', 1
);

INSERT INTO `sys_role`(`id`, `name`, `remark`, `tenant_id`) VALUES
  (1, 'ROLE_SUPER_ADMIN', '平台超级管理员（拥有全部菜单与 API）', 1);

INSERT INTO `sys_user`(
  `id`, `username`, `password`, `enabled`, `gender`, `avatar`, `phone`, `email`,
  `status`, `province`, `city`, `address`, `tenant_id`, `org_id`
) VALUES (
  1, 'superadmin', '123456', 1, 'M', NULL, '13800000000', 'superadmin@skylark.local',
  'ACTIVE', 'ZJ', 'HZ', 'Skylark HQ', 1, 1
);

INSERT INTO `sys_user_role`(`user_id`, `role_id`) VALUES (1, 1);

INSERT INTO `sys_tenant_admin_binding`(`tenant_id`, `user_id`, `role_id`) VALUES (1, 1, 1);

-- Default data domain: ALL (view all data), and bind it to super admin role.
INSERT INTO `sys_data_domain`(
  `id`, `name`, `code`, `type`, `scope_value`, `custom_sql`, `description`, `tenant_id`, `enabled`
) VALUES (
  1, '全部数据', 'ALL', 'ALL', NULL, NULL, '查看全部数据', 1, 1
);

INSERT INTO `sys_role_data_domain`(`role_id`, `data_domain_id`) VALUES (1, 1);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`)
SELECT 1, m.`id` FROM `sys_menu` m;

INSERT INTO `sys_role_api`(`role_id`, `api_id`)
SELECT 1, a.`id` FROM `sys_api` a;

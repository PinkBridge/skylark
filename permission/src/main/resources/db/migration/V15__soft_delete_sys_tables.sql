-- Soft delete for sys_* tables: add is_delete flag (0/1).
-- Deletions should update is_delete=1 instead of physical delete.
-- Also adjust key unique indexes to include is_delete so that soft-deleted rows don't block re-creation.

-- Core entity tables
ALTER TABLE `sys_tenant` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `expire_time`;
ALTER TABLE `sys_organization` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `tenant_id`;
ALTER TABLE `sys_user` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `org_id`;
ALTER TABLE `sys_role` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `tenant_id`;
ALTER TABLE `sys_resource` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `tenant_id`;
ALTER TABLE `sys_data_domain` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `enabled`;

-- System config / audit / logs
ALTER TABLE `sys_platform_config` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `description`;
ALTER TABLE `sys_permission_audit` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_operation_log` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_login_log` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_whitelist` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `enabled`;

-- Menu/API resources
ALTER TABLE `sys_menu` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `module_key`;
ALTER TABLE `sys_api` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag' AFTER `module_key`;

-- Join / binding tables (still add flag for consistency)
ALTER TABLE `sys_user_role` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_role_menu` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_role_api` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_role_data_domain` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_tenant_admin_binding` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';

-- Newly added sys_* tables in this repo
ALTER TABLE `sys_platform_init_state` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';
ALTER TABLE `sys_oauth_client_meta` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'soft delete flag';

-- Adjust unique indexes for soft delete.
-- sys_user: username must be unique among non-deleted records.
ALTER TABLE `sys_user` DROP INDEX `uk_username`;
CREATE UNIQUE INDEX `uk_username_is_delete` ON `sys_user`(`username`, `is_delete`);

-- sys_tenant: code/domain must be unique among non-deleted records.
ALTER TABLE `sys_tenant` DROP INDEX `uk_tenant_code`;
ALTER TABLE `sys_tenant` DROP INDEX `uk_tenant_domain`;
CREATE UNIQUE INDEX `uk_tenant_code_is_delete` ON `sys_tenant`(`code`, `is_delete`);
CREATE UNIQUE INDEX `uk_tenant_domain_is_delete` ON `sys_tenant`(`domain`, `is_delete`);

-- sys_organization: code must be unique among non-deleted records.
ALTER TABLE `sys_organization` DROP INDEX `uk_code`;
CREATE UNIQUE INDEX `uk_org_code_is_delete` ON `sys_organization`(`code`, `is_delete`);

-- sys_role: (name, tenant_id) unique among non-deleted records.
ALTER TABLE `sys_role` DROP INDEX `uk_role_name_tenant`;
CREATE UNIQUE INDEX `uk_role_name_tenant_is_delete` ON `sys_role`(`name`, `tenant_id`, `is_delete`);

-- sys_data_domain: (code, tenant_id) unique among non-deleted records.
ALTER TABLE `sys_data_domain` DROP INDEX `uk_code_tenant`;
CREATE UNIQUE INDEX `uk_data_domain_code_tenant_is_delete` ON `sys_data_domain`(`code`, `tenant_id`, `is_delete`);

-- sys_api: (method, path) unique among non-deleted records.
ALTER TABLE `sys_api` DROP INDEX `uk_api`;
CREATE UNIQUE INDEX `uk_api_is_delete` ON `sys_api`(`method`, `path`, `is_delete`);

-- sys_platform_config: config_key unique among non-deleted records.
ALTER TABLE `sys_platform_config` DROP INDEX `uk_platform_config_key`;
CREATE UNIQUE INDEX `uk_platform_config_key_is_delete` ON `sys_platform_config`(`config_key`, `is_delete`);


-- =============================================================================
-- 彻底清空 permission 库对象（表结构一并删除）并移除 Flyway 历史。
-- 停止 permission 容器后执行；再启动容器，Flyway 将按当前 JAR 内 migration 全量重建。
-- 使用：docker exec -i skylark-mysql mysql -uroot -p<ROOT密码> < 本文件.sql
-- 库名默认 skylark_permission（与 docker-compose 中 PERMISSION_DB_NAME 一致）。
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `skylark_permission`;
USE `skylark_permission`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_role_api`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_role_data_domain`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_tenant_admin_binding`;
DROP TABLE IF EXISTS `sys_permission_audit`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_login_log`;
DROP TABLE IF EXISTS `sys_resource`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_organization`;
DROP TABLE IF EXISTS `sys_tenant`;
DROP TABLE IF EXISTS `sys_api`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_whitelist`;
DROP TABLE IF EXISTS `sys_platform_config`;
DROP TABLE IF EXISTS `sys_data_domain`;

DROP TABLE IF EXISTS `oauth_refresh_token`;
DROP TABLE IF EXISTS `oauth_code`;
DROP TABLE IF EXISTS `oauth_approvals`;
DROP TABLE IF EXISTS `oauth_client_details`;

DROP TABLE IF EXISTS `flyway_schema_history`;

SET FOREIGN_KEY_CHECKS = 1;

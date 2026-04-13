-- 6) 平台级参数配置

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `sys_platform_config`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_platform_config` (`config_key`, `config_value`, `value_type`, `description`) VALUES
  ('operation.log.writes_only', 'false', 'BOOL',
   'When true, only write requests (POST, PUT, PATCH, DELETE, etc.) are recorded in the operation log; GET, HEAD, and TRACE are excluded.'),
  ('tenant.domain_sync_client_ids', 'permission-web,iot-web', 'STRING',
   'OAuth clientIds whose redirect URIs are updated when a tenant domain changes. Comma-separated.');

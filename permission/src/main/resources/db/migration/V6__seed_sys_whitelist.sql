-- 5) 系统白名单（与现行放行策略一致）

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `sys_whitelist`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `sys_whitelist`(`method`, `path`, `remark`, `enabled`) VALUES
  ('ALL', '/oauth/**', 'OAuth2 认证相关接口', 1),
  ('ALL', '/error', '错误处理接口', 1),
  ('GET', '/api/permission/tenants/domain/**', '租户域名查询（登录前解析租户）', 1);

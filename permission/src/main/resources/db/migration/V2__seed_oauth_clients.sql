-- 1) OAuth2 客户端：permission-web（权限中心 Web）、iot-web（IoT Web）
-- 执行前请已通过 manual 脚本或空库完成清空；此处仍 TRUNCATE 以保证可重复跑 repair 场景

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `oauth_client_details`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `oauth_client_details`(
  `client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`,
  `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`,
  `additional_information`, `autoapprove`
) VALUES
  (
    'permission-web',
    'permission',
    '112233',
    'all',
    'authorization_code,refresh_token',
    'http://localhost:9528/home,http://localhost/home',
    NULL,
    604800,
    86400,
    NULL,
    'true'
  ),
  (
    'iot-web',
    'iot',
    '112233',
    'all',
    'authorization_code,refresh_token',
    'http://localhost:8081/home',
    NULL,
    604800,
    86400,
    NULL,
    'true'
  );

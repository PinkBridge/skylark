-- Restore OAuth2 clients removed by V23:
-- business-web UI client + business-service service-to-service client.
--
-- Redirect URIs must contain the OAuth callback destinations used by business-web shells.
INSERT INTO `oauth_client_details`(
  `client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`,
  `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`,
  `additional_information`, `autoapprove`
) VALUES
  (
    'business-web',
    'business',
    '112233',
    'all',
    'authorization_code,refresh_token,password',
    'http://127.0.0.1:9531/home,http://localhost:9531/home',
    NULL,
    604800,
    86400,
    NULL,
    'true'
  ),
  (
    'business-service',
    'business',
    '112233',
    'all',
    'client_credentials',
    NULL,
    NULL,
    604800,
    NULL,
    NULL,
    'true'
  )
ON DUPLICATE KEY UPDATE
  `resource_ids` = VALUES(`resource_ids`),
  `client_secret` = VALUES(`client_secret`),
  `scope` = VALUES(`scope`),
  `authorized_grant_types` = VALUES(`authorized_grant_types`),
  `web_server_redirect_uri` = VALUES(`web_server_redirect_uri`),
  `authorities` = VALUES(`authorities`),
  `access_token_validity` = VALUES(`access_token_validity`),
  `refresh_token_validity` = VALUES(`refresh_token_validity`),
  `additional_information` = VALUES(`additional_information`),
  `autoapprove` = VALUES(`autoapprove`);

INSERT INTO `sys_oauth_client_meta` (`client_id`, `name`, `port`)
VALUES
  ('business-web', 'Business Web', 9531),
  ('business-service', 'Business Service', 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `port` = VALUES(`port`);

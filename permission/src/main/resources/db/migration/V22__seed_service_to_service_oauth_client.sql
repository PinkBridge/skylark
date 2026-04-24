-- Generic OAuth2 client for service-to-service calls (client_credentials only).
-- Used by internal starters (e.g. skylark-authz-spring-boot-starter) to pull snapshots from permission.

INSERT INTO `oauth_client_details`(
  `client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`,
  `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`,
  `additional_information`, `autoapprove`
) VALUES (
  'skylark-service',
  'internal',
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


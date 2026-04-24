-- skylark-demo-web devServer runs on 9529 (see web/apps/skylark-demo-web/vue.config.js).
-- OAuth2 authorization_code requires redirect_uri to match a registered value exactly.
-- V2 only seeded localhost:9528 for permission-web.

UPDATE `oauth_client_details`
SET `web_server_redirect_uri` = TRIM(BOTH ',' FROM CONCAT(
  COALESCE(`web_server_redirect_uri`, ''),
  CASE
    WHEN `web_server_redirect_uri` IS NULL OR `web_server_redirect_uri` NOT LIKE '%localhost:9529/home%'
    THEN ',http://localhost:9529/home'
    ELSE ''
  END,
  CASE
    WHEN `web_server_redirect_uri` IS NULL OR `web_server_redirect_uri` NOT LIKE '%127.0.0.1:9529/home%'
    THEN ',http://127.0.0.1:9529/home'
    ELSE ''
  END,
  CASE
    WHEN `web_server_redirect_uri` IS NULL OR `web_server_redirect_uri` NOT LIKE '%127.0.0.1:9528/home%'
    THEN ',http://127.0.0.1:9528/home'
    ELSE ''
  END
))
WHERE `client_id` = 'permission-web';

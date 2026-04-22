-- 表单登录与白名单：避免自定义 RBAC 链路遗漏 /login；iot-web 增加常用本地回调地址

INSERT INTO sys_whitelist(`method`, `path`, `remark`, `enabled`)
SELECT 'ALL', '/login', 'Spring Security 表单登录 GET/POST', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_whitelist WHERE `method` = 'ALL' AND `path` = '/login');

UPDATE oauth_client_details
SET web_server_redirect_uri = CASE
  WHEN web_server_redirect_uri LIKE '%localhost:9531%' THEN web_server_redirect_uri
  ELSE TRIM(BOTH ',' FROM CONCAT(web_server_redirect_uri, ',http://localhost:9531/home'))
END
WHERE client_id = 'iot-web';

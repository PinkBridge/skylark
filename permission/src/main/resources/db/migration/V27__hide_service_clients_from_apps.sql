-- By default, hide non-UI clients from /apps (e.g. service-to-service clients with port=0).
UPDATE `sys_oauth_client_meta`
SET `is_show` = 0
WHERE `port` = 0;


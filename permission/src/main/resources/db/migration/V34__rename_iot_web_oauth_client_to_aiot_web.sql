-- Rename UI OAuth client iot-web -> aiot-web (match web/apps/aiot-web), open /apps entry, UI port 9532 (docker-compose aiot-web).
UPDATE `oauth_client_details`
SET `client_id` = 'aiot-web',
    `web_server_redirect_uri` = 'http://localhost:9532/home,http://127.0.0.1:9532/home'
WHERE `client_id` = 'iot-web';

UPDATE `sys_oauth_client_meta`
SET `client_id` = 'aiot-web',
    `is_open` = 1,
    `port` = 9532
WHERE `client_id` = 'iot-web';

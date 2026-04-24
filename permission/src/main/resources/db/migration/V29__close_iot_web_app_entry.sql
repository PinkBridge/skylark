-- Close iot-web app entry by default (cannot click to open from /apps).
UPDATE `sys_oauth_client_meta`
SET `is_open` = 0
WHERE `client_id` = 'iot-web';


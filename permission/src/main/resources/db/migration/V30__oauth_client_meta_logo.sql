-- App logo for /apps page.
ALTER TABLE `sys_oauth_client_meta`
  ADD COLUMN `logo` VARCHAR(512) DEFAULT NULL COMMENT 'logo url (absolute or /api/... download url)' AFTER `name`;


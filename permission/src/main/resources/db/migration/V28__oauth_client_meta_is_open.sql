-- Whether an app entry is open for navigation.
ALTER TABLE `sys_oauth_client_meta`
  ADD COLUMN `is_open` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'whether user can click to open the app' AFTER `terminal_type`;

CREATE INDEX `idx_oauth_client_meta_open`
  ON `sys_oauth_client_meta`(`is_open`);


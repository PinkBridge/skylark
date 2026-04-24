-- Apps page data source: sys_oauth_client_meta.
-- Add presentation fields: show flag, sort order, terminal type.

ALTER TABLE `sys_oauth_client_meta`
  ADD COLUMN `is_show` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'whether to show in /apps' AFTER `port`,
  ADD COLUMN `sort` INT NOT NULL DEFAULT 0 COMMENT 'sort order (asc)' AFTER `is_show`,
  ADD COLUMN `terminal_type` VARCHAR(32) NOT NULL DEFAULT 'PC-web' COMMENT 'PC-web / Mobile-web' AFTER `sort`;

CREATE INDEX `idx_oauth_client_meta_show_terminal_sort`
  ON `sys_oauth_client_meta`(`is_show`, `terminal_type`, `sort`, `client_id`);


-- Add org/user audit fields; rename deleted -> is_delete

ALTER TABLE `biz_blog_post`
  ADD COLUMN `org_id` BIGINT DEFAULT NULL COMMENT 'Organization id' AFTER `tenant_id`,
  ADD COLUMN `create_user` VARCHAR(64) DEFAULT NULL COMMENT 'Creator username' AFTER `tags`,
  ADD COLUMN `update_user` VARCHAR(64) DEFAULT NULL COMMENT 'Updater username' AFTER `create_user`;

ALTER TABLE `biz_blog_post`
  CHANGE COLUMN `deleted` `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag';

-- Indexes: replace deleted index with is_delete index
ALTER TABLE `biz_blog_post`
  DROP INDEX `idx_blog_post_tenant_deleted`,
  ADD KEY `idx_blog_post_tenant_is_delete` (`tenant_id`, `is_delete`);


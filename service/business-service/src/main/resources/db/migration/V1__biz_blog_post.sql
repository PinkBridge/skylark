CREATE TABLE IF NOT EXISTS `biz_blog_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant id (from X-Tenant-Id header)',
  `title` VARCHAR(200) NOT NULL COMMENT 'Title',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT 'Summary',
  `content` MEDIUMTEXT NOT NULL COMMENT 'Content (plain/markdown)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ARCHIVED',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT 'Comma-separated tags',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_blog_post_tenant_time` (`tenant_id`, `create_time`),
  KEY `idx_blog_post_tenant_status` (`tenant_id`, `status`),
  KEY `idx_blog_post_tenant_deleted` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Business blog posts';


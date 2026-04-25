-- Fix wrong app_code for blog posts APIs.
-- V31 was already applied on existing databases, so this patch is done as a new migration.

UPDATE `sys_api`
SET `app_code` = 'business-web'
WHERE `id` IN (10101, 10102, 10103, 10104)
  AND `path` LIKE '/api/business-service/blog/posts%'
  AND `app_code` <> 'business-web';


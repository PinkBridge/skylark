-- business-web: ensure the top-level menu group exists so /menus/*/tree can render a sidebar.
-- Some environments may have leaf nodes (e.g. 博客文章) inserted with a parent_id referencing a missing group.

INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `app_code`
) VALUES (
  2000, NULL,
  '业务管理', JSON_OBJECT('zh', '业务管理', 'en', 'Business'),
  '/biz', 'Briefcase',
  40, 0, 'menu',
  'biz.group', 'business', 'business-web'
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `name_i18n` = VALUES(`name_i18n`),
  `path` = VALUES(`path`),
  `icon` = VALUES(`icon`),
  `sort` = VALUES(`sort`),
  `hidden` = VALUES(`hidden`),
  `type` = VALUES(`type`),
  `permlabel` = VALUES(`permlabel`),
  `module_key` = VALUES(`module_key`),
  `app_code` = VALUES(`app_code`);


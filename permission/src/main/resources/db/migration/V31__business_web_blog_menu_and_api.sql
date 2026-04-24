-- business-web: 博客文章菜单（app_code=business-web，供 /menus/me/tree?app= 使用）
-- business-service: 博客文章 API（app_code=business-service，与 SKYLARK_AUTHZ_APP_CODE 一致，供 RBAC snapshot）
-- 绑定平台超级管理员角色（id=1）

-- 1) sys_api（Ant 路径与 BlogPostController 一致）
INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`, `is_delete`, `app_code`)
SELECT 10101, 'GET', '/api/business-service/blog/posts/**', 'biz.blog.posts.detail', 'business.blog', 0, 'business-service'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_api` WHERE `id` = 10101);

INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`, `is_delete`, `app_code`)
SELECT 10102, 'POST', '/api/business-service/blog/posts', 'biz.blog.posts.new', 'business.blog', 0, 'business-service'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_api` WHERE `id` = 10102);

INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`, `is_delete`, `app_code`)
SELECT 10103, 'PUT', '/api/business-service/blog/posts/**', 'biz.blog.posts.edit', 'business.blog', 0, 'business-service'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_api` WHERE `id` = 10103);

INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`, `is_delete`, `app_code`)
SELECT 10104, 'DELETE', '/api/business-service/blog/posts/**', 'biz.blog.posts.delete', 'business.blog', 0, 'business-service'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_api` WHERE `id` = 10104);

-- 2) sys_menu（与 business-web 前端 v-permission / 路由对齐）
INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  2001, 2000,
  '博客文章', JSON_OBJECT('zh', '博客文章', 'en', 'Blog posts'),
  '/blog/posts', 'Reading',
  1, 0, 'menu',
  'biz.blog.posts.view', 'business.blog', 0, 'business-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2001);

INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  2011, 2001, '新建', JSON_OBJECT('zh', '新建', 'en', 'New'), '', NULL, 1, 1, 'button',
  'biz.blog.posts.new', 'business.blog', 0, 'business-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2011);

INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  2012, 2001, '详情', JSON_OBJECT('zh', '详情', 'en', 'Detail'), '', NULL, 2, 1, 'button',
  'biz.blog.posts.detail', 'business.blog', 0, 'business-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2012);

INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  2013, 2001, '编辑', JSON_OBJECT('zh', '编辑', 'en', 'Edit'), '', NULL, 3, 1, 'button',
  'biz.blog.posts.edit', 'business.blog', 0, 'business-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2013);

INSERT INTO `sys_menu`(
  `id`, `parent_id`, `name`, `name_i18n`, `path`, `icon`, `sort`, `hidden`, `type`, `permlabel`, `module_key`, `is_delete`, `app_code`
) SELECT
  2014, 2001, '删除', JSON_OBJECT('zh', '删除', 'en', 'Delete'), '', NULL, 4, 1, 'button',
  'biz.blog.posts.delete', 'business.blog', 0, 'business-web'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2014);

-- 3) 绑定超级管理员（role_id=1）
-- V20 在 V5 之后执行，存量库可能缺少「业务管理」分组(2000) 的角色关联
INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 2000, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2000 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2000 AND `is_delete` = 0);

INSERT INTO `sys_role_api`(`role_id`, `api_id`, `is_delete`)
SELECT 1, 10101, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_api` WHERE `role_id` = 1 AND `api_id` = 10101 AND `is_delete` = 0);

INSERT INTO `sys_role_api`(`role_id`, `api_id`, `is_delete`)
SELECT 1, 10102, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_api` WHERE `role_id` = 1 AND `api_id` = 10102 AND `is_delete` = 0);

INSERT INTO `sys_role_api`(`role_id`, `api_id`, `is_delete`)
SELECT 1, 10103, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_api` WHERE `role_id` = 1 AND `api_id` = 10103 AND `is_delete` = 0);

INSERT INTO `sys_role_api`(`role_id`, `api_id`, `is_delete`)
SELECT 1, 10104, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_api` WHERE `role_id` = 1 AND `api_id` = 10104 AND `is_delete` = 0);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 2001, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2001 AND `is_delete` = 0);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 2011, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2011 AND `is_delete` = 0);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 2012, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2012 AND `is_delete` = 0);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 2013, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2013 AND `is_delete` = 0);

INSERT INTO `sys_role_menu`(`role_id`, `menu_id`, `is_delete`)
SELECT 1, 2014, 0 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1 AND `is_delete` = 0)
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2014 AND `is_delete` = 0);

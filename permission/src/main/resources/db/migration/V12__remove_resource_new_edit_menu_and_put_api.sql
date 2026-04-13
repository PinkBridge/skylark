-- 资源管理：去掉「新建」「编辑」菜单按钮；去掉 PUT 资源 API（上传仍使用 POST + perm.resources.new）
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (3901, 3903);
DELETE FROM `sys_menu` WHERE `id` IN (3901, 3903);

DELETE FROM `sys_role_api` WHERE `api_id` = 10027;
DELETE FROM `sys_api` WHERE `id` = 10027;

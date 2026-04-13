-- 角色绑定数据域（与 menus:bind / apis:bind 同级，使用 perm.roles.api）

INSERT INTO `sys_api`(`id`, `method`, `path`, `permlabel`, `module_key`) VALUES
  (10061, 'POST', '/api/permission/roles/*/data-domains:bind', 'perm.roles.api', 'permission.role');

INSERT INTO `sys_role_api` (`role_id`, `api_id`)
SELECT `r`.`id`, 10061
FROM `sys_role` `r`
WHERE `r`.`name` = 'ROLE_SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_api` `ra`
    WHERE `ra`.`role_id` = `r`.`id` AND `ra`.`api_id` = 10061
  );

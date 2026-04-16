-- Add admin reset-password permission (API + menu button) and bind to super admin.

-- 1) sys_api
INSERT INTO sys_api(id, method, path, permlabel, module_key, is_delete)
SELECT 10062, 'PUT', '/api/permission/users/*/password:reset', 'perm.users.password.reset', 'permission.user', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_api WHERE id = 10062);

-- 2) sys_menu button under Users (1002)
INSERT INTO sys_menu(id, parent_id, name, name_i18n, path, icon, sort, hidden, type, permlabel, module_key, is_delete)
SELECT 3205, 1002, '重置密码', JSON_OBJECT('zh','重置密码','en','Reset password'), '', NULL, 5, 1, 'button',
       'perm.users.password.reset', 'permission.user', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 3205);

-- 3) Bind to super admin role (id=1) if present
INSERT INTO sys_role_api(role_id, api_id, is_delete)
SELECT 1, 10062, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM sys_role WHERE id = 1 AND is_delete = 0)
  AND NOT EXISTS (SELECT 1 FROM sys_role_api WHERE role_id = 1 AND api_id = 10062);

INSERT INTO sys_role_menu(role_id, menu_id, is_delete)
SELECT 1, 3205, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM sys_role WHERE id = 1 AND is_delete = 0)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 3205);


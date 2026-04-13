-- 数据域菜单更名为「数据域管理」，同步 name 与 name_i18n（已执行过 V4 的环境）
UPDATE `sys_menu`
SET `name` = '数据域管理',
    `name_i18n` = JSON_OBJECT('zh', '数据域管理', 'en', 'Data domains')
WHERE `id` = 1016;

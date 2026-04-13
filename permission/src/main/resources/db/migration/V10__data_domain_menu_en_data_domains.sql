-- 菜单 1016 英文保持为 Data domains（与历史文案一致）
UPDATE `sys_menu`
SET `name_i18n` = JSON_OBJECT('zh', '数据域管理', 'en', 'Data domains')
WHERE `id` = 1016;

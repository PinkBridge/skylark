-- 菜单所属前端应用（与 OAuth client / 壳应用对应，如 permission-web、iot-web）
ALTER TABLE `sys_menu`
  ADD COLUMN `app_code` VARCHAR(64) NOT NULL DEFAULT 'permission-web'
    COMMENT '所属前端应用标识，如 permission-web、iot-web'
    AFTER `module_key`;

-- 显式初始化存量数据（与默认值一致，便于审计）
UPDATE `sys_menu` SET `app_code` = 'permission-web' WHERE `is_delete` = 0;

CREATE INDEX `idx_menu_app_code` ON `sys_menu` (`app_code`);

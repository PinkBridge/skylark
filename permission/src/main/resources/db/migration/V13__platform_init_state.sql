-- Platform initialization state.
-- We keep default seed data (tenant/org/superadmin) from earlier migrations,
-- and use this table to drive a first-run "configuration wizard".

CREATE TABLE IF NOT EXISTS `sys_platform_init_state` (
  `id` BIGINT NOT NULL COMMENT 'singleton row id, always 1',
  `status` VARCHAR(20) NOT NULL COMMENT 'PENDING|COMPLETED',
  `finished_at` DATETIME DEFAULT NULL COMMENT 'completion time',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Platform initialization state';

-- Ensure singleton row exists. MySQL: INSERT ... SELECT ... WHERE NOT EXISTS.
INSERT INTO `sys_platform_init_state` (`id`, `status`, `finished_at`)
SELECT 1, 'PENDING', NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_platform_init_state` WHERE `id` = 1);


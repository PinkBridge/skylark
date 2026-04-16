-- OAuth client meta table: client_id -> (name, port)

CREATE TABLE IF NOT EXISTS `sys_oauth_client_meta` (
  `client_id` VARCHAR(256) NOT NULL COMMENT 'OAuth client_id',
  `name` VARCHAR(128) NOT NULL COMMENT 'Display name',
  `port` INT NOT NULL COMMENT 'UI port for redirect_uri composition',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth client meta (name, port)';

-- Seed common clients.
INSERT INTO `sys_oauth_client_meta` (`client_id`, `name`, `port`)
VALUES
  ('permission-web', 'Permission System', 9527),
  ('iot-web', 'IOT System', 9528)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `port` = VALUES(`port`);


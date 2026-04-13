-- 全量表结构（无业务数据）。与旧版多脚本合并后的基线。

CREATE TABLE IF NOT EXISTS `oauth_client_details` (
  `client_id` VARCHAR(256) NOT NULL COMMENT '客户端ID',
  `resource_ids` VARCHAR(256) DEFAULT NULL COMMENT '资源ID列表',
  `client_secret` VARCHAR(256) DEFAULT NULL COMMENT '客户端密钥',
  `scope` VARCHAR(256) DEFAULT NULL COMMENT '作用域',
  `authorized_grant_types` VARCHAR(256) DEFAULT NULL COMMENT '授权模式',
  `web_server_redirect_uri` VARCHAR(256) DEFAULT NULL COMMENT '重定向URI',
  `authorities` VARCHAR(256) DEFAULT NULL COMMENT '权限',
  `access_token_validity` INT(11) DEFAULT NULL COMMENT '访问令牌有效期（秒）',
  `refresh_token_validity` INT(11) DEFAULT NULL COMMENT '刷新令牌有效期（秒）',
  `additional_information` VARCHAR(4096) DEFAULT NULL COMMENT '附加信息',
  `autoapprove` VARCHAR(256) DEFAULT NULL COMMENT '自动授权',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2客户端详情表';

CREATE TABLE IF NOT EXISTS `oauth_code` (
  `code` VARCHAR(256) NOT NULL COMMENT '授权码',
  `authentication` BLOB COMMENT '授权信息',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2授权码表';

CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
  `token_id` VARCHAR(256) DEFAULT NULL COMMENT 'Token ID',
  `token` BLOB COMMENT 'Refresh Token 序列化数据',
  `authentication` BLOB COMMENT '授权信息',
  KEY `token_id` (`token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2刷新令牌表';

CREATE TABLE IF NOT EXISTS `oauth_approvals` (
  `userId` VARCHAR(256) DEFAULT NULL COMMENT '用户ID',
  `clientId` VARCHAR(256) DEFAULT NULL COMMENT '客户端ID',
  `scope` VARCHAR(256) DEFAULT NULL COMMENT '作用域',
  `status` VARCHAR(10) DEFAULT NULL COMMENT '状态',
  `expiresAt` DATETIME DEFAULT NULL COMMENT '过期时间',
  `lastModifiedAt` DATETIME DEFAULT NULL COMMENT '最后修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2用户授权表';

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `gender` VARCHAR(1) DEFAULT NULL COMMENT '性别：M-男，F-女',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '照片URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃，LOCKED-锁定',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `tenant_id` BIGINT(20) DEFAULT NULL COMMENT '租户ID',
  `org_id` BIGINT(20) DEFAULT NULL COMMENT '组织ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT DEFAULT NULL,
  `name` VARCHAR(100) NOT NULL,
  `name_i18n` JSON NULL COMMENT '多语言名称 {"zh":"...","en":"..."}',
  `path` VARCHAR(200) DEFAULT NULL,
  `icon` VARCHAR(100) DEFAULT NULL,
  `sort` INT DEFAULT 0,
  `hidden` TINYINT(1) DEFAULT 0,
  `type` VARCHAR(20) DEFAULT 'menu' COMMENT '类型：菜单、按钮',
  `permlabel` VARCHAR(100) DEFAULT NULL COMMENT '权限标签',
  `module_key` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单';

CREATE TABLE IF NOT EXISTS `sys_api` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `method` VARCHAR(10) NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `permlabel` VARCHAR(100) NOT NULL,
  `module_key` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api` (`method`,`path`),
  KEY `idx_permlabel` (`permlabel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API 资源';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `tenant_id` BIGINT(20) DEFAULT NULL COMMENT '租户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name_tenant` (`name`, `tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `menu_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联';

CREATE TABLE IF NOT EXISTS `sys_role_api` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `api_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_api` (`role_id`,`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-API关联';

CREATE TABLE IF NOT EXISTS `sys_role_data_domain` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `data_domain_id` BIGINT NOT NULL COMMENT '数据域ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_data_domain` (`role_id`,`data_domain_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_data_domain_id` (`data_domain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-数据域关联';

CREATE TABLE IF NOT EXISTS `sys_whitelist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
  `path` VARCHAR(255) NOT NULL COMMENT 'API路径',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_method_path` (`method`, `path`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统白名单表';

CREATE TABLE IF NOT EXISTS `sys_organization` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `name` VARCHAR(100) NOT NULL COMMENT '组织名称',
  `code` VARCHAR(50) NOT NULL COMMENT '组织编码（唯一）',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父组织ID',
  `type` VARCHAR(20) DEFAULT 'DEPARTMENT' COMMENT '组织类型',
  `level` INT(11) DEFAULT 1 COMMENT '层级',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `leader` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `tenant_id` BIGINT(20) DEFAULT NULL COMMENT '租户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统组织表';

CREATE TABLE IF NOT EXISTS `sys_tenant` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '租户名称',
  `system_name` VARCHAR(100) DEFAULT NULL COMMENT '系统名称',
  `code` VARCHAR(50) NOT NULL COMMENT '租户编码（唯一）',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
  `domain` VARCHAR(200) DEFAULT NULL COMMENT '租户访问域名',
  `logo` VARCHAR(500) DEFAULT NULL COMMENT '租户Logo地址',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '租户地址',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '租户描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `expire_time` DATETIME DEFAULT NULL COMMENT '租户到期时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`code`),
  UNIQUE KEY `uk_tenant_domain` (`domain`),
  KEY `idx_tenant_name` (`name`),
  KEY `idx_tenant_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统租户表';

CREATE TABLE IF NOT EXISTS `sys_resource` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_type` VARCHAR(20) DEFAULT 'OTHER' COMMENT '文件类型',
  `file_size` BIGINT(20) DEFAULT 0 COMMENT '文件大小（字节）',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  `url` VARCHAR(500) DEFAULT NULL COMMENT '访问URL',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `tenant_id` BIGINT(20) DEFAULT NULL COMMENT '租户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统资源表';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
  `login_ip` VARCHAR(128) DEFAULT NULL COMMENT '登录IP',
  `login_location` VARCHAR(256) DEFAULT NULL COMMENT '登录地点',
  `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '用户代理',
  `login_status` VARCHAR(16) DEFAULT NULL COMMENT '登录状态',
  `login_message` VARCHAR(512) DEFAULT NULL COMMENT '登录消息',
  `login_time` DATETIME DEFAULT NULL COMMENT '登录时间',
  `tenant_id` BIGINT(20) DEFAULT NULL COMMENT '租户ID',
  `client_id` VARCHAR(256) DEFAULT NULL COMMENT 'OAuth2客户端ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_login_status` (`login_status`),
  KEY `idx_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS `sys_data_domain` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '数据域名称',
  `code` VARCHAR(50) NOT NULL COMMENT '数据域编码',
  `type` VARCHAR(20) NOT NULL COMMENT '数据范围类型',
  `scope_value` TEXT COMMENT '数据域范围值',
  `custom_sql` TEXT COMMENT '自定义SQL规则',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `tenant_id` BIGINT(20) DEFAULT NULL COMMENT '租户ID',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_tenant` (`code`, `tenant_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_type` (`type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据域配置表';

CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT DEFAULT NULL,
  `user_id` BIGINT DEFAULT NULL,
  `username` VARCHAR(128) DEFAULT NULL,
  `http_method` VARCHAR(16) NOT NULL,
  `request_uri` VARCHAR(1024) NOT NULL,
  `http_status` INT NOT NULL,
  `duration_ms` BIGINT NOT NULL,
  `client_ip` VARCHAR(64) DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_oplog_tenant_created` (`tenant_id`, `created_at`),
  KEY `idx_oplog_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

CREATE TABLE IF NOT EXISTS `sys_platform_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(2048) NOT NULL COMMENT '配置值',
  `value_type` VARCHAR(32) NOT NULL DEFAULT 'STRING' COMMENT '值类型',
  `description` VARCHAR(512) NULL COMMENT '说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台级参数配置';

CREATE TABLE IF NOT EXISTS `sys_permission_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NULL,
  `operator` VARCHAR(64) NULL COMMENT '操作人用户名',
  `action` VARCHAR(64) NOT NULL COMMENT '动作',
  `detail` TEXT NULL COMMENT '详情',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_permission_audit_tenant_time` (`tenant_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限审计';

CREATE TABLE IF NOT EXISTS `sys_tenant_admin_binding` (
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `user_id` BIGINT NOT NULL COMMENT '绑定用户ID',
  `role_id` BIGINT NOT NULL COMMENT '权限上限角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`tenant_id`),
  KEY `idx_tenant_admin_user` (`user_id`),
  KEY `idx_tenant_admin_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户管理员绑定';

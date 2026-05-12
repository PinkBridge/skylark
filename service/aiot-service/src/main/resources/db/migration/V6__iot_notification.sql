CREATE TABLE IF NOT EXISTS iot_notification_channel (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,                -- DINGTALK / WECOM / FEISHU / EMAIL / SMS
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    config_json TEXT NOT NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_iot_notify_channel_tenant_enabled (tenant_id, enabled)
);

CREATE TABLE IF NOT EXISTS iot_notification_subscription (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    channel_id BIGINT NOT NULL,
    name VARCHAR(128) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    device_group_key VARCHAR(64) NOT NULL,
    event_types TEXT NOT NULL,
    filter_json TEXT NULL,
    template_json TEXT NOT NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_iot_notify_sub_tenant_group (tenant_id, device_group_key),
    KEY idx_iot_notify_sub_tenant_channel (tenant_id, channel_id),
    KEY idx_iot_notify_sub_channel (channel_id)
);

CREATE TABLE IF NOT EXISTS iot_notification_delivery (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    subscription_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,              -- success / failed / dead
    attempts INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP NULL,
    last_error VARCHAR(1024) NULL,
    http_status INT NULL,
    payload_snapshot TEXT NULL,
    rendered_title VARCHAR(256) NULL,
    rendered_body TEXT NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_iot_notify_delivery_retry (status, next_retry_at, attempts),
    KEY idx_iot_notify_delivery_tenant_ct (tenant_id, created_at)
);


CREATE TABLE IF NOT EXISTS iot_alarm_rule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    device_group_key VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    source_type VARCHAR(16) NOT NULL,         -- PROPERTY / EVENT
    severity VARCHAR(16) NOT NULL,            -- HIGH / MEDIUM / LOW
    trigger_mode VARCHAR(16) NOT NULL,        -- INSTANT / DURATION
    duration_seconds INT NOT NULL DEFAULT 0,
    recovery_mode VARCHAR(16) NOT NULL,       -- AUTO / MANUAL
    dedup_mode VARCHAR(16) NOT NULL,          -- SINGLE_ACTIVE / EVERY_TRIGGER
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    condition_json TEXT NOT NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alarm_rule_tenant_group (tenant_id, device_group_key),
    KEY idx_alarm_rule_tenant_enabled (tenant_id, enabled)
);

CREATE TABLE IF NOT EXISTS iot_alarm (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    rule_id BIGINT NOT NULL,
    device_group_key VARCHAR(64) NOT NULL,
    product_key VARCHAR(64) NOT NULL,
    device_key VARCHAR(64) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,              -- ACTIVE / RECOVERED / CLOSED
    first_triggered_at TIMESTAMP NULL,
    last_triggered_at TIMESTAMP NULL,
    recovered_at TIMESTAMP NULL,
    trigger_count INT NOT NULL DEFAULT 0,
    evidence_json TEXT NULL,
    last_event_id VARCHAR(64) NULL,
    last_event_type VARCHAR(128) NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_alarm_tenant_status_time (tenant_id, status, last_triggered_at),
    KEY idx_alarm_rule_device (rule_id, product_key, device_key),
    KEY idx_alarm_group_device (device_group_key, product_key, device_key)
);

CREATE TABLE IF NOT EXISTS iot_alarm_eval_state (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    rule_id BIGINT NOT NULL,
    device_group_key VARCHAR(64) NOT NULL,
    product_key VARCHAR(64) NOT NULL,
    device_key VARCHAR(64) NOT NULL,
    condition_met_since TIMESTAMP NULL,
    last_seen_at TIMESTAMP NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alarm_eval_state (tenant_id, rule_id, product_key, device_key),
    KEY idx_alarm_eval_state_tenant_group (tenant_id, device_group_key)
);


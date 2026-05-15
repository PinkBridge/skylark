-- Multiple outbound notify channels per tenant (email SMTP profiles, SMS vendor profiles).
-- Alarm rules reference chosen channel rows via iot_alarm_notify_config.email_notify_channel_id / sms_notify_channel_id.

CREATE TABLE IF NOT EXISTS iot_notify_channel (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    org_id BIGINT NULL,
    channel_kind VARCHAR(16) NOT NULL COMMENT 'EMAIL | SMS',
    provider VARCHAR(32) NOT NULL COMMENT 'SMTP | ALIYUN | GOOGLE',
    name VARCHAR(128) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    config_json LONGTEXT NOT NULL COMMENT 'Provider-specific JSON (secrets stored here; mask in API)',
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    create_user VARCHAR(64) NULL,
    update_user VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notify_ch_tenant_kind (tenant_id, channel_kind),
    KEY idx_notify_ch_tenant (tenant_id)
);

-- Migrate legacy single-row-per-tenant settings into channel rows (if table exists from V8).
INSERT INTO iot_notify_channel (tenant_id, org_id, channel_kind, provider, name, enabled, config_json, is_delete, created_at, updated_at)
SELECT
    s.tenant_id,
    s.org_id,
    'EMAIL',
    'SMTP',
    CONCAT('Migrated SMTP #', s.tenant_id),
    IFNULL(s.mail_enabled, 0),
    JSON_OBJECT(
        'smtpHost', s.smtp_host,
        'smtpPort', s.smtp_port,
        'smtpSsl', IFNULL(s.smtp_ssl, 1),
        'smtpUsername', s.smtp_username,
        'smtpPassword', s.smtp_password,
        'mailFrom', s.mail_from,
        'mailFromDisplay', s.mail_from_display
    ),
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM iot_notify_settings s
WHERE s.is_delete = 0
  AND s.smtp_host IS NOT NULL
  AND TRIM(s.smtp_host) <> '';

INSERT INTO iot_notify_channel (tenant_id, org_id, channel_kind, provider, name, enabled, config_json, is_delete, created_at, updated_at)
SELECT
    s.tenant_id,
    s.org_id,
    'SMS',
    IFNULL(NULLIF(TRIM(UPPER(s.sms_provider)), ''), 'ALIYUN'),
    CONCAT('Migrated SMS #', s.tenant_id),
    IFNULL(s.sms_enabled, 0),
    JSON_OBJECT(
        'smsAccessKeyId', s.sms_access_key_id,
        'smsAccessKeySecret', s.sms_access_key_secret,
        'smsSignName', s.sms_sign_name,
        'smsTemplateCode', s.sms_template_code,
        'smsRegionId', IFNULL(NULLIF(TRIM(s.sms_region_id), ''), 'cn-hangzhou')
    ),
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM iot_notify_settings s
WHERE s.is_delete = 0
  AND s.sms_access_key_id IS NOT NULL
  AND TRIM(s.sms_access_key_id) <> '';

ALTER TABLE iot_alarm_notify_config
    ADD COLUMN email_notify_channel_id BIGINT NULL AFTER sms_enabled,
    ADD COLUMN sms_notify_channel_id BIGINT NULL AFTER email_notify_channel_id,
    ADD CONSTRAINT fk_alarm_notify_cfg_email_ch FOREIGN KEY (email_notify_channel_id) REFERENCES iot_notify_channel (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_alarm_notify_cfg_sms_ch FOREIGN KEY (sms_notify_channel_id) REFERENCES iot_notify_channel (id) ON DELETE SET NULL;

UPDATE iot_alarm_notify_config c
JOIN (
    SELECT tenant_id, MIN(id) AS cid
    FROM iot_notify_channel
    WHERE is_delete = 0 AND channel_kind = 'EMAIL' AND enabled = 1
    GROUP BY tenant_id
) x ON x.tenant_id = c.tenant_id
SET c.email_notify_channel_id = x.cid
WHERE c.is_delete = 0 AND c.email_enabled = 1;

UPDATE iot_alarm_notify_config c
JOIN (
    SELECT tenant_id, MIN(id) AS cid
    FROM iot_notify_channel
    WHERE is_delete = 0 AND channel_kind = 'SMS' AND enabled = 1
    GROUP BY tenant_id
) x ON x.tenant_id = c.tenant_id
SET c.sms_notify_channel_id = x.cid
WHERE c.is_delete = 0 AND c.sms_enabled = 1;

DROP TABLE IF EXISTS iot_notify_settings;

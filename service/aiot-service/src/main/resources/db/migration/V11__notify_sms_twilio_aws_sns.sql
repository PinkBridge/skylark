-- Replace legacy Google CCAI SMS provider with Twilio; clear config so operators re-enter credentials.
UPDATE iot_notify_channel
SET provider = 'TWILIO',
    enabled = 0,
    config_json = '{}',
    updated_at = CURRENT_TIMESTAMP
WHERE channel_kind = 'SMS'
  AND UPPER(TRIM(IFNULL(provider, ''))) = 'GOOGLE'
  AND IFNULL(is_delete, 0) = 0;

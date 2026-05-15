-- Remove IoT message-subscription (notification) tables; feature retired (see V6 for original DDL).
DROP TABLE IF EXISTS iot_notification_delivery;
DROP TABLE IF EXISTS iot_notification_subscription;
DROP TABLE IF EXISTS iot_notification_channel;

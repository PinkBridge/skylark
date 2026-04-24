-- Remove business-web/business-service OAuth clients seeded by V21.
-- Keep generic service client (skylark-service) from V22.

DELETE FROM `oauth_client_details`
WHERE `client_id` IN ('business-web', 'business-service');

DELETE FROM `sys_oauth_client_meta`
WHERE `client_id` IN ('business-web', 'business-service');


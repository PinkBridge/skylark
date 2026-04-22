# skylark-authz-spring-boot-starter

Business service local authorization starter for Skylark.

## What it does

- Periodically pulls **RBAC snapshot** from `permission` service using OAuth2 `client_credentials`
- Caches `role -> (method + pathPattern)` in-memory
- Enforces access via a Spring MVC `HandlerInterceptor` (403 when not allowed, 401 when unauthenticated)

## How to use

Add dependency (example):

```xml
<dependency>
  <groupId>cn.skylark</groupId>
  <artifactId>skylark-authz-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Configure in `application.yml`:

```yaml
skylark:
  authz:
    enabled: true
    app-code: demo-service
    permission-base-url: http://permission:8080
    client-id: demo-service
    client-secret: your-secret
    sync-fixed-delay-ms: 30000
    ignore-paths:
      - /actuator/**
      - /v3/api-docs/**
      - /swagger-ui/**
```

## Required permission service endpoints

The starter expects:

- `POST {permissionBaseUrl}{tokenPath}` (default: `/oauth/token`) supporting `client_credentials`
- `GET  {permissionBaseUrl}{snapshotPath}` (default: `/api/permission/authz/snapshot`)
  - query params: `appCode`, `sinceVersion`
  - response body: `cn.skylark.authz.starter.model.RbacSnapshotResponse`


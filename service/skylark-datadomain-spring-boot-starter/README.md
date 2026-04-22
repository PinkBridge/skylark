# skylark-datadomain-spring-boot-starter

Reusable **tenant + data domain (row scope)** support for Skylark business services (Spring Boot + MyBatis).

## What it provides

1. **`TenantHeaderFilter`**  
   Reads `X-Tenant-Id` (configurable) into `DataDomainContext` and clears context at end of request.

2. **`DataScopeHandlerInterceptor`** (optional)  
   After Spring Security authentication, resolves `ResolvedDataScopeDTO` and writes flags into `DataDomainContext`.

3. **`DataDomainMybatisInterceptor`** (optional, requires MyBatis on classpath)  
   Rewrites SQL similarly to `permission`’s `TenantInterceptor`:
   - Adds `tenant_id = ?` for configured tenant tables
   - Applies row scope (`org_id IN (...)`, `id IN (...)`, `self column = userId`) based on `DataDomainContext` + `row-scope-rules`

## Configuration

```yaml
skylark:
  datadomain:
    enabled: true
    tenant-header: X-Tenant-Id
    # Remote resolve (requires permission HTTP API — see below)
    resolve-data-scope: false
    permission-base-url: http://permission:8080
    client-id: your-service-client
    client-secret: your-secret
    data-scope-resolve-path: /api/permission/internal/resolved-data-scope
    resolve-cache-ttl-ms: 60000
    tenant-id-column: tenant_id
    tenant-tables:
      - sys_user
      - sys_organization
    exclude-tables:
      - sys_tenant
    all-platform-skip-tenant-select-tables:
      - sys_user
    row-scope-rules:
      - table: sys_user
        org-id-column: org_id
        self-user-id-column: id
      - table: sys_organization
        org-ids-on-primary-key: true
```

> YAML property names use kebab-case; they map to Java `orgIdsOnPrimaryKey` (`org-ids-on-primary-key`).

## Permission service contract (when `resolve-data-scope=true`)

Implement:

`GET {permissionBaseUrl}{data-scope-resolve-path}?username=...&tenantId=...`

Response JSON compatible with `cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO`, including optional `userId` for `SELF` semantics.

Authentication: **Bearer token** from `client_credentials` (`client-id` / `client-secret`).

## Dependency

```xml
<dependency>
  <groupId>cn.skylark</groupId>
  <artifactId>skylark-datadomain-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Also add **MyBatis** + **JSQLParser** is already pulled transitively by this starter.

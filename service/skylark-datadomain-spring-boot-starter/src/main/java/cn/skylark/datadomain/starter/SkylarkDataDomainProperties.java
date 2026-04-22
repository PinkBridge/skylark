package cn.skylark.datadomain.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "skylark.datadomain")
@Data
public class SkylarkDataDomainProperties {

  private boolean enabled = true;

  /** HTTP header for tenant id (gateway or client sets this). */
  private String tenantHeader = "X-Tenant-Id";

  /**
   * When true, after Spring Security authentication, resolve data scope into {@link cn.skylark.datadomain.starter.context.DataDomainContext}.
   */
  private boolean resolveDataScope = false;

  /** Base URL of permission service (for remote resolve). */
  private String permissionBaseUrl;

  private String tokenPath = "/oauth/token";

  /**
   * GET endpoint returning {@link cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO}.
   * Expected query params: username, tenantId (optional).
   */
  private String dataScopeResolvePath = "/api/permission/internal/resolved-data-scope";

  private String clientId;
  private String clientSecret;

  /** Cache TTL for remote resolved data scope per (tenantId, username). */
  private long resolveCacheTtlMs = 60_000L;

  /** MyBatis: column name for tenant id rewrite. */
  private String tenantIdColumn = "tenant_id";

  /** Tables that receive automatic tenant_id condition (whitelist). Empty = no tenant rewrite. */
  private List<String> tenantTables = new ArrayList<>();

  /** Tables excluded from tenant rewrite entirely. */
  private List<String> excludeTables = new ArrayList<>();

  /**
   * When allPlatform is true, skip tenant filter for plain single-table SELECT on these tables only
   * (same idea as permission {@code TenantInterceptor}).
   */
  private List<String> allPlatformSkipTenantSelectTables = new ArrayList<>();

  /** Row-scope rules per logical table name (lowercase match). */
  private List<RowScopeTableRule> rowScopeRules = new ArrayList<>();

  @Data
  public static class RowScopeTableRule {
    /** Table name without schema, case-insensitive match. */
    private String table;
    /** If true, append {@code id IN (orgIds)} for SELECT/UPDATE/DELETE where. */
    private boolean orgIdsOnPrimaryKey;
    /** If set, append {@code column IN (orgIds)}. Ignored if orgIdsOnPrimaryKey is true. */
    private String orgIdColumn;
    /** When selfOnly, append {@code column = currentUserId}. */
    private String selfUserIdColumn;
  }
}

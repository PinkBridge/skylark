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
   * Optional HTTP header for organization id (gateway or client sets this).
   * Parsed into {@link cn.skylark.datadomain.starter.context.DataDomainContext} for optional audit/org auto-fill.
   */
  private String orgHeader = "X-Org-Id";

  /**
   * When true, MyBatis interceptor will append missing audit columns on INSERT/UPDATE for tenant tables:
   * {@link #createUserColumn}, {@link #updateUserColumn}, {@link #orgIdColumn} (org id on INSERT by default;
   * UPDATE controlled by {@link #applyOrgIdOnUpdate}).
   * <p>
   * Note: this only fills columns that are <b>missing</b> from the SQL column list / UPDATE SET clause
   * (same behavior as {@code tenant_id} auto-append).
   */
  private boolean autoFillAuditFields = false;

  /** DB column name for creator username. */
  private String createUserColumn = "create_user";

  /** DB column name for updater username. */
  private String updateUserColumn = "update_user";

  /** DB column name for organization id. */
  private String orgIdColumn = "org_id";

  /**
   * When true, UPDATE statements may also set {@link #orgIdColumn} from {@link cn.skylark.datadomain.starter.context.DataDomainContext}
   * if the column is not already present in SET clause.
   * <p>
   * Default false: org is usually immutable after insert.
   */
  private boolean applyOrgIdOnUpdate = false;

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

  /**
   * When true (default), any {@link #tenantTables} entry that is not in {@link #rowScopeExcludeTables}
   * automatically gets row-level data scope using {@link #orgIdColumn} and {@link #createUserColumn}
   * (as login name for {@code selfOnly}), unless overridden by {@link #rowScopeRules}.
   * <p>
   * Tables without those columns must be listed in {@link #rowScopeExcludeTables} or they may get invalid SQL.
   */
  private boolean defaultRowScope = true;

  /**
   * Tenant tables that skip row-scope rewriting (tenant_id / soft-delete still apply).
   * Use when a table has no {@link #orgIdColumn} / {@link #createUserColumn} or must not be filtered.
   */
  private List<String> rowScopeExcludeTables = new ArrayList<>();

  /**
   * Optional per-table overrides (non-standard org column, {@code id IN (orgIds)}, numeric self column, etc.).
   * When a table is listed here, this rule replaces the {@link #defaultRowScope} template for that table.
   */
  private List<RowScopeTableRule> rowScopeRules = new ArrayList<>();

  /**
   * Soft delete support.
   * <p>
   * When enabled, interceptor will:
   * - append {@link #softDeleteColumn} = {@link #softDeleteActiveValue} to SELECT/UPDATE/DELETE (if not already present)
   * - optionally rewrite DELETE into UPDATE setting {@link #softDeleteColumn} = {@link #softDeleteDeletedValue}
   */
  private SoftDeleteProperties softDelete = new SoftDeleteProperties();

  @Data
  public static class SoftDeleteProperties {
    /** Enable soft-delete SQL rewriting. Default false (opt-in). */
    private boolean enabled = false;

    /** Column used as soft-delete flag. */
    private String column = "is_delete";

    /** Value meaning "not deleted". */
    private long activeValue = 0L;

    /** Value meaning "deleted". */
    private long deletedValue = 1L;

    /**
     * Tables to apply soft-delete on. Empty means fallback to {@link SkylarkDataDomainProperties#tenantTables}.
     * Table name match is case-insensitive (normalized).
     */
    private List<String> tables = new ArrayList<>();

    /** When true, rewrite DELETE statements into UPDATE setting deletedValue. */
    private boolean rewriteDeleteToUpdate = true;
  }

  @Data
  public static class RowScopeTableRule {
    /** Table name without schema, case-insensitive match. */
    private String table;
    /** If true, append {@code id IN (orgIds)} for SELECT/UPDATE/DELETE where. */
    private boolean orgIdsOnPrimaryKey;
    /** If set, append {@code column IN (orgIds)}. Ignored if orgIdsOnPrimaryKey is true. */
    private String orgIdColumn;
    /**
     * When {@code selfOnly}, append {@code column = resolvedUserId} (numeric).
     * Use for PK / {@code user_id} style columns; not for {@code create_user} varchar.
     */
    private String selfUserIdColumn;
    /**
     * When {@code selfOnly}, append {@code column = currentUsername} (string, SQL-quoted).
     * Use for audit columns like {@code create_user} that store the login name.
     * If both this and {@link #selfUserIdColumn} are set, both predicates may apply (combined with OR).
     */
    private String selfUsernameColumn;
  }
}

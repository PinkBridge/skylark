package cn.skylark.permission.authorization.support;

/**
 * 平台级参数 {@code sys_platform_config.config_key} 常量。
 */
public final class PlatformConfigKeys {

  private PlatformConfigKeys() {
  }

  /**
   * 为 true 时操作日志仅记录写请求（POST/PUT/PATCH/DELETE），不记录 GET/HEAD/TRACE。
   */
  public static final String OPERATION_LOG_WRITES_ONLY = "operation.log.writes_only";

  /**
   * 当租户域名变更时，需要同步更新 redirect_uri 的 OAuth clientId 列表（逗号分隔）。
   */
  public static final String TENANT_DOMAIN_SYNC_CLIENT_IDS = "tenant.domain_sync_client_ids";
}

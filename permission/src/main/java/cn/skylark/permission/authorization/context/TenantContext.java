package cn.skylark.permission.authorization.context;

/**
 * 租户上下文
 * 使用ThreadLocal存储当前请求的租户ID
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
public class TenantContext {

  private static final ThreadLocal<Long> TENANT_ID_HOLDER = new ThreadLocal<>();

  /**
   * 当前用户数据域解析为「全平台」时置位；用于对 sys_user 等 SELECT 跳过租户行条件，与 dataScope.allPlatform 一致。
   */
  private static final ThreadLocal<Boolean> ALL_PLATFORM_DATA_SCOPE = new ThreadLocal<>();

  /**
   * 设置当前租户ID
   *
   * @param tenantId 租户ID
   */
  public static void setTenantId(Long tenantId) {
    TENANT_ID_HOLDER.set(tenantId);
  }

  /**
   * 获取当前租户ID
   *
   * @return 租户ID，如果未设置则返回null
   */
  public static Long getTenantId() {
    return TENANT_ID_HOLDER.get();
  }

  public static void setAllPlatformDataScope(boolean allPlatform) {
    ALL_PLATFORM_DATA_SCOPE.set(allPlatform);
  }

  public static boolean isAllPlatformDataScope() {
    return Boolean.TRUE.equals(ALL_PLATFORM_DATA_SCOPE.get());
  }

  /**
   * 清除当前租户ID
   * 在请求结束后调用，防止内存泄漏
   */
  public static void clear() {
    TENANT_ID_HOLDER.remove();
    ALL_PLATFORM_DATA_SCOPE.remove();
  }
}


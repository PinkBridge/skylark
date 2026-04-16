package cn.skylark.permission.authorization.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

  private static final ThreadLocal<Boolean> DATA_SCOPE_WHOLE_TENANT = new ThreadLocal<>();

  private static final ThreadLocal<Boolean> DATA_SCOPE_SELF_ONLY = new ThreadLocal<>();

  private static final ThreadLocal<List<Long>> DATA_SCOPE_ORG_IDS = new ThreadLocal<>();

  private static final ThreadLocal<Long> DATA_SCOPE_USER_ID = new ThreadLocal<>();

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

  public static void setDataScopeWholeTenant(boolean wholeTenant) {
    DATA_SCOPE_WHOLE_TENANT.set(wholeTenant);
  }

  public static boolean isDataScopeWholeTenant() {
    return Boolean.TRUE.equals(DATA_SCOPE_WHOLE_TENANT.get());
  }

  public static void setDataScopeSelfOnly(boolean selfOnly) {
    DATA_SCOPE_SELF_ONLY.set(selfOnly);
  }

  public static boolean isDataScopeSelfOnly() {
    return Boolean.TRUE.equals(DATA_SCOPE_SELF_ONLY.get());
  }

  public static void setDataScopeOrgIds(List<Long> orgIds) {
    if (orgIds == null || orgIds.isEmpty()) {
      DATA_SCOPE_ORG_IDS.remove();
      return;
    }
    DATA_SCOPE_ORG_IDS.set(Collections.unmodifiableList(new ArrayList<>(orgIds)));
  }

  public static List<Long> getDataScopeOrgIds() {
    return DATA_SCOPE_ORG_IDS.get();
  }

  public static void setDataScopeUserId(Long userId) {
    if (userId == null) {
      DATA_SCOPE_USER_ID.remove();
      return;
    }
    DATA_SCOPE_USER_ID.set(userId);
  }

  public static Long getDataScopeUserId() {
    return DATA_SCOPE_USER_ID.get();
  }

  /**
   * 清除当前租户ID
   * 在请求结束后调用，防止内存泄漏
   */
  public static void clear() {
    TENANT_ID_HOLDER.remove();
    ALL_PLATFORM_DATA_SCOPE.remove();
    DATA_SCOPE_WHOLE_TENANT.remove();
    DATA_SCOPE_SELF_ONLY.remove();
    DATA_SCOPE_ORG_IDS.remove();
    DATA_SCOPE_USER_ID.remove();
  }
}


package cn.skylark.datadomain.starter.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Request-scoped context for tenant id and resolved row-level data scope.
 * Must be cleared at the end of each request (see filters in this starter).
 */
public final class DataDomainContext {

  private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
  private static final ThreadLocal<Long> ORG_ID = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> ALL_PLATFORM = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> WHOLE_TENANT = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> SELF_ONLY = new ThreadLocal<>();
  private static final ThreadLocal<List<Long>> ORG_IDS = new ThreadLocal<>();
  private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

  public static void setTenantId(Long tenantId) {
    TENANT_ID.set(tenantId);
  }

  public static Long getTenantId() {
    return TENANT_ID.get();
  }

  public static void setOrgId(Long orgId) {
    if (orgId == null) {
      ORG_ID.remove();
      return;
    }
    ORG_ID.set(orgId);
  }

  public static Long getOrgId() {
    return ORG_ID.get();
  }

  public static void setAllPlatformDataScope(boolean v) {
    ALL_PLATFORM.set(v);
  }

  public static boolean isAllPlatformDataScope() {
    return Boolean.TRUE.equals(ALL_PLATFORM.get());
  }

  public static void setDataScopeWholeTenant(boolean v) {
    WHOLE_TENANT.set(v);
  }

  public static boolean isDataScopeWholeTenant() {
    return Boolean.TRUE.equals(WHOLE_TENANT.get());
  }

  public static void setDataScopeSelfOnly(boolean v) {
    SELF_ONLY.set(v);
  }

  public static boolean isDataScopeSelfOnly() {
    return Boolean.TRUE.equals(SELF_ONLY.get());
  }

  public static void setDataScopeOrgIds(List<Long> orgIds) {
    if (orgIds == null || orgIds.isEmpty()) {
      ORG_IDS.remove();
      return;
    }
    ORG_IDS.set(Collections.unmodifiableList(new ArrayList<>(orgIds)));
  }

  public static List<Long> getDataScopeOrgIds() {
    return ORG_IDS.get();
  }

  public static void setDataScopeUserId(Long userId) {
    if (userId == null) {
      USER_ID.remove();
      return;
    }
    USER_ID.set(userId);
  }

  public static Long getDataScopeUserId() {
    return USER_ID.get();
  }

  public static void clear() {
    TENANT_ID.remove();
    ORG_ID.remove();
    ALL_PLATFORM.remove();
    WHOLE_TENANT.remove();
    SELF_ONLY.remove();
    ORG_IDS.remove();
    USER_ID.remove();
  }
}

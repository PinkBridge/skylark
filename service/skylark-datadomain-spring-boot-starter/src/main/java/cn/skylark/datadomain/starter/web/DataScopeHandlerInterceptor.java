package cn.skylark.datadomain.starter.web;

import cn.skylark.datadomain.starter.SkylarkDataDomainProperties;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO;
import cn.skylark.datadomain.starter.resolve.DataScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * After Spring Security, resolves data scope into {@link DataDomainContext} for MyBatis layer.
 */
@RequiredArgsConstructor
public class DataScopeHandlerInterceptor implements HandlerInterceptor {

  private final SkylarkDataDomainProperties props;
  private final DataScopeResolver resolver;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!props.isEnabled()) {
      return true;
    }
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return true;
    }

    // 1) Populate tenant/org/user claims from Authentication (if present).
    applyClaimsFromAuthentication(auth);

    // 2) Optional: remote resolve row-level data scope.
    if (props.isResolveDataScope()) {
      ResolvedDataScopeDTO dto = resolver.resolve(auth);
      DataDomainContext.setAllPlatformDataScope(dto.isAllPlatform());
      DataDomainContext.setDataScopeWholeTenant(dto.isWholeTenant());
      DataDomainContext.setDataScopeSelfOnly(dto.isSelfOnly());
      DataDomainContext.setDataScopeOrgIds(dto.getOrgIds());
      DataDomainContext.setDataScopeUserId(dto.getUserId());
    }
    return true;
  }

  private static void applyClaimsFromAuthentication(Authentication auth) {
    Object details = auth.getDetails();
    if (!(details instanceof Map)) {
      return;
    }
    Map<?, ?> m = (Map<?, ?>) details;
    // Keep header-provided tenant id as higher priority
    if (DataDomainContext.getTenantId() == null) {
      Long tid = asLong(m.get("tenant_id"));
      if (tid != null) {
        DataDomainContext.setTenantId(tid);
      }
    }
    if (DataDomainContext.getOrgId() == null) {
      Long orgId = asLong(m.get("org_id"));
      if (orgId != null) {
        DataDomainContext.setOrgId(orgId);
      }
    }
    if (DataDomainContext.getDataScopeUserId() == null) {
      Long uid = asLong(m.get("user_id"));
      if (uid != null) {
        DataDomainContext.setDataScopeUserId(uid);
      }
    }
    // org_ids claim -> row scope orgIds (if not already resolved)
    if (DataDomainContext.getDataScopeOrgIds() == null || DataDomainContext.getDataScopeOrgIds().isEmpty()) {
      Object v = m.get("org_ids");
      List<Long> ids = asLongList(v);
      if (ids != null && !ids.isEmpty()) {
        DataDomainContext.setDataScopeOrgIds(ids);
      }
    }
  }

  private static Long asLong(Object v) {
    if (v == null) {
      return null;
    }
    if (v instanceof Number) {
      return ((Number) v).longValue();
    }
    if (v instanceof String) {
      String s = ((String) v).trim();
      if (!StringUtils.hasText(s)) {
        return null;
      }
      try {
        return Long.parseLong(s);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  private static List<Long> asLongList(Object v) {
    if (v == null) {
      return null;
    }
    if (v instanceof List) {
      List<?> raw = (List<?>) v;
      java.util.ArrayList<Long> out = new java.util.ArrayList<>();
      for (Object o : raw) {
        Long n = asLong(o);
        if (n != null) {
          out.add(n);
        }
      }
      return out;
    }
    // allow single value
    Long one = asLong(v);
    if (one != null) {
      return java.util.Collections.singletonList(one);
    }
    return null;
  }
}

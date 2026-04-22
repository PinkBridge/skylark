package cn.skylark.datadomain.starter.web;

import cn.skylark.datadomain.starter.SkylarkDataDomainProperties;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO;
import cn.skylark.datadomain.starter.resolve.DataScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * After Spring Security, resolves data scope into {@link DataDomainContext} for MyBatis layer.
 */
@RequiredArgsConstructor
public class DataScopeHandlerInterceptor implements HandlerInterceptor {

  private final SkylarkDataDomainProperties props;
  private final DataScopeResolver resolver;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!props.isEnabled() || !props.isResolveDataScope()) {
      return true;
    }
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return true;
    }
    ResolvedDataScopeDTO dto = resolver.resolve(auth);
    DataDomainContext.setAllPlatformDataScope(dto.isAllPlatform());
    DataDomainContext.setDataScopeWholeTenant(dto.isWholeTenant());
    DataDomainContext.setDataScopeSelfOnly(dto.isSelfOnly());
    DataDomainContext.setDataScopeOrgIds(dto.getOrgIds());
    DataDomainContext.setDataScopeUserId(dto.getUserId());
    return true;
  }
}

package cn.skylark.permission.authentication.filter;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.ResolvedDataScopeDTO;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.service.DataDomainResolutionService;
import cn.skylark.permission.authorization.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * JWT 之后根据当前用户角色解析数据域；若含 type=ALL，则置 {@link TenantContext#setAllPlatformDataScope(boolean)}，
 * 供 {@code TenantInterceptor} 对 {@code TENANT_TABLES} 中单表 SELECT 跳过租户行条件（与 /users/me 的 dataScope 一致）。
 */
@Component
public class AllPlatformDataScopeWebFilter extends OncePerRequestFilter {

  @Resource
  private UserService userService;
  @Resource
  private DataDomainResolutionService dataDomainResolutionService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = resolveUsername(auth);
    if (username != null) {
      SysUser user = userService.findByUsername(username);
      if (user != null) {
        List<SysRole> roles = userService.roles(user.getId());
        ResolvedDataScopeDTO scope = dataDomainResolutionService.resolve(user, roles);
        if (scope.isAllPlatform()) {
          TenantContext.setAllPlatformDataScope(true);
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private static String resolveUsername(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      return null;
    }
    Object p = auth.getPrincipal();
    if (p instanceof UserDetails) {
      return ((UserDetails) p).getUsername();
    }
    if (p instanceof String) {
      return (String) p;
    }
    return null;
  }
}

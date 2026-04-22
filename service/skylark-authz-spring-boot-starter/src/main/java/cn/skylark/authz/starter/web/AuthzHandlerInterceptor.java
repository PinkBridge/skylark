package cn.skylark.authz.starter.web;

import cn.skylark.authz.starter.SkylarkAuthzProperties;
import cn.skylark.authz.starter.core.AuthzCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class AuthzHandlerInterceptor implements HandlerInterceptor {

  private final SkylarkAuthzProperties props;
  private final AuthzCache cache;
  private final PathMatcher ignoreMatcher = new AntPathMatcher();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (!props.isEnabled()) {
      return true;
    }
    String path = request.getRequestURI();
    if (isIgnored(path, props.getIgnorePaths())) {
      return true;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      if (props.isRequireAuthentication()) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
      }
      return true;
    }

    Set<String> roles = extractRoles(auth);
    boolean allowed = cache.getState().isAllowed(roles, request.getMethod(), path);
    if (!allowed) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      return false;
    }
    return true;
  }

  private boolean isIgnored(String path, List<String> ignorePaths) {
    if (ignorePaths == null || ignorePaths.isEmpty()) {
      return false;
    }
    for (String pattern : ignorePaths) {
      if (pattern == null || pattern.trim().isEmpty()) {
        continue;
      }
      if (ignoreMatcher.match(pattern.trim(), path)) {
        return true;
      }
    }
    return false;
  }

  private static Set<String> extractRoles(Authentication auth) {
    Set<String> roles = new HashSet<>();
    if (auth.getAuthorities() == null) {
      return roles;
    }
    for (GrantedAuthority a : auth.getAuthorities()) {
      if (a != null && a.getAuthority() != null && !a.getAuthority().trim().isEmpty()) {
        roles.add(a.getAuthority().trim());
      }
    }
    return roles;
  }
}


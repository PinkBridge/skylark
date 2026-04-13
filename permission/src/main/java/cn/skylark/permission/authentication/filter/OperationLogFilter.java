package cn.skylark.permission.authentication.filter;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.service.OperationLogService;
import cn.skylark.permission.authorization.service.PlatformConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 记录已鉴权 API 调用的技术信息：时间、用户、方法、URI、状态码、耗时等。
 * 放在 JWT 过滤器之后，以便解析到登录用户。
 */
@Component
@Slf4j
public class OperationLogFilter extends OncePerRequestFilter {

  private static final int MAX_URI_LEN = 1024;

  @Resource
  private OperationLogService operationLogService;

  @Resource
  private PlatformConfigService platformConfigService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String uri = request.getRequestURI();
    if (!uri.startsWith("/api/")) {
      return true;
    }
    if (uri.startsWith("/api/permission/operation-logs")) {
      return true;
    }
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    long start = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - start;
      try {
        Long tenantId = TenantContext.getTenantId();
        String username = resolveUsername();
        String method = request.getMethod();
        if (platformConfigService.isOperationLogWritesOnly() && isReadHttpMethod(method)) {
          return;
        }
        String fullUri = buildRequestUri(request);
        int status = response.getStatus();
        String clientIp = clientIp(request);
        operationLogService.recordAsync(tenantId, username, method, fullUri, status, duration, clientIp);
      } catch (Exception e) {
        log.debug("operation log scheduling failed", e);
      }
    }
  }

  private static boolean isReadHttpMethod(String method) {
    if (method == null) {
      return true;
    }
    String m = method.toUpperCase();
    return "GET".equals(m) || "HEAD".equals(m) || "TRACE".equals(m);
  }

  private static String resolveUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return auth.getName();
  }

  private static String buildRequestUri(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String qs = request.getQueryString();
    if (qs != null && !qs.isEmpty()) {
      uri = uri + "?" + qs;
    }
    if (uri.length() > MAX_URI_LEN) {
      return uri.substring(0, MAX_URI_LEN);
    }
    return uri;
  }

  private static String clientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}

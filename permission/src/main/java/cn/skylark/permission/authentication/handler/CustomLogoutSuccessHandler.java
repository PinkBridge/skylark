package cn.skylark.permission.authentication.handler;

import cn.skylark.permission.authentication.service.LogoutService;
import com.alibaba.fastjson.JSON;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yaomianwei
 * @since 2025/11/3
 */
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

  @Resource
  private LogoutService logoutService;

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                              Authentication authentication) throws IOException, ServletException {
    String result;
    String accessToken = extractAccessToken(request);
    try {
      if (accessToken != null && !accessToken.isEmpty()) {
        if (logoutService.logout(accessToken)) {
          result = buildSuccessResult("Logout successful.Refresh token has been deleted.");
        } else {
          result = buildSuccessResult("Logout completed.Refresh token may not exist or already deleted.");
        }
      } else {
        result = buildSuccessResult("Logout successful.");
      }
    } catch (Exception e) {
      result = buildFailResult(e.getMessage());
    }

    // Best-effort: clear common server-side session cookies (often HttpOnly) so browsers drop them immediately.
    for (String cookie : buildClearCookieHeaders(request)) {
      response.addHeader("Set-Cookie", cookie);
    }

    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(result);
  }

  private String buildSuccessResult(String msg) {
    Map<String, Object> result = new HashMap<>(16);
    result.put("success", true);
    result.put("message", msg);
    return JSON.toJSONString(result);
  }

  private String buildFailResult(String msg) {
    Map<String, Object> result = new HashMap<>(16);
    result.put("success", false);
    result.put("message", msg);
    return JSON.toJSONString(result);
  }

  private String extractAccessToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }

    String accessToken = request.getParameter("access_token");
    if (accessToken != null && !accessToken.isEmpty()) {
      return accessToken;
    }
    return null;
  }

  /**
   * Build Set-Cookie header values to expire cookies for the current host/path.
   * Note: HttpOnly cookies cannot be cleared from JS; this is the server-side counterpart.
   */
  private static List<String> buildClearCookieHeaders(HttpServletRequest request) {
    String host = request.getServerName();
    String ctx = request.getContextPath() == null ? "" : request.getContextPath();
    String path = (ctx == null || ctx.isEmpty()) ? "/" : ctx;

    Set<String> names = new LinkedHashSet<>();
    names.add("JSESSIONID");
    names.add("SESSION");
    names.add("remember-me");

    List<String> out = new ArrayList<>();
    for (String name : names) {
      // Try common variants: host-only + domain + with/without Secure + SameSite=None
      out.add(expireCookie(name, path, null, false, false));
      out.add(expireCookie(name, path, null, true, false));
      if (host != null && !host.isEmpty() && !"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equals(host)) {
        out.add(expireCookie(name, path, host, false, false));
        out.add(expireCookie(name, path, host, true, false));
        out.add(expireCookie(name, path, "." + host, false, false));
        out.add(expireCookie(name, path, "." + host, true, false));
      }
      // Some deployments use SameSite=None; Secure cookies
      out.add(expireCookie(name, path, null, true, true));
      if (host != null && !host.isEmpty() && !"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equals(host)) {
        out.add(expireCookie(name, path, host, true, true));
        out.add(expireCookie(name, path, "." + host, true, true));
      }
    }
    return out;
  }

  private static String expireCookie(String name, String path, String domain, boolean secure, boolean sameSiteNone) {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append("=").append("; Path=").append(path == null || path.isEmpty() ? "/" : path);
    sb.append("; Max-Age=0");
    sb.append("; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    if (domain != null && !domain.isEmpty()) {
      sb.append("; Domain=").append(domain);
    }
    if (secure) {
      sb.append("; Secure");
    }
    if (sameSiteNone) {
      sb.append("; SameSite=None");
    }
    sb.append("; HttpOnly");
    return sb.toString();
  }
}

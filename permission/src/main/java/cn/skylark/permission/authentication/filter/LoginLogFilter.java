package cn.skylark.permission.authentication.filter;

import cn.skylark.permission.authentication.OauthConfig;
import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.entity.SysLoginLog;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.service.LoginLogService;
import cn.skylark.permission.authorization.service.UserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 登录日志过滤器
 * 拦截 /oauth/token 请求，记录登录成功和失败的日志
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Component
@Slf4j
public class LoginLogFilter extends OncePerRequestFilter {

  @Resource
  private LoginLogService loginLogService;

  @Resource
  private UserService userService;

  @Resource
  private OauthConfig oauthConfig;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    if (!isLoginRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
    try {
      // 获取请求参数
      String username = request.getParameter("username");
      String grantType = request.getParameter("grant_type");

      if ("password".equals(grantType) || "authorization_code".equals(grantType)) {
        // 获取请求信息
        String loginIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String clientId = extractClientId(request);

        // 执行过滤器链
        filterChain.doFilter(request, responseWrapper);

        // 读取响应内容
        byte[] responseBody = responseWrapper.getContentAsByteArray();
        String responseBodyStr = new String(responseBody, StandardCharsets.UTF_8);

        // 对于 authorization_code 模式，从响应中的 access_token 提取用户名
        if ("authorization_code".equals(grantType) && !StringUtils.hasText(username)) {
          username = extractUsernameFromTokenResponse(responseBodyStr);
        }

        // 记录登录日志
        recordLoginLog(username, loginIp, userAgent, responseWrapper.getStatus(), responseBodyStr, grantType, clientId);
      } else {
        filterChain.doFilter(request, responseWrapper);
        responseWrapper.copyBodyToResponse();
        return;
      }


    } catch (Exception e) {
      log.error("Error recording login log", e);
      // 即使记录日志失败，也不影响正常请求流程
    } finally {
      // 将响应内容写回原始响应
      responseWrapper.copyBodyToResponse();
    }
  }

  /**
   * 判断是否是登录请求
   */
  private boolean isLoginRequest(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    return "/oauth/token".equals(requestURI) &&
            "POST".equalsIgnoreCase(request.getMethod());
  }

  /**
   * 提取客户端ID（OAuth2 client_id）
   * 支持从 Basic Authentication 或请求参数中获取
   */
  private String extractClientId(HttpServletRequest request) {
    // 1. 优先从请求参数获取
    String clientId = request.getParameter("client_id");
    if (StringUtils.hasText(clientId)) {
      return clientId;
    }

    // 2. 从 Basic Authentication 中获取（username 字段通常是 client_id）
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Basic ")) {
      try {
        String base64Credentials = authorization.substring("Basic ".length());
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
        // credentials 格式：client_id:client_secret
        String[] parts = credentials.split(":", 2);
        if (parts.length > 0 && StringUtils.hasText(parts[0])) {
          return parts[0];
        }
      } catch (Exception e) {
        log.debug("Failed to extract client_id from Basic Auth", e);
      }
    }

    return null;
  }

  /**
   * 获取客户端IP地址
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 如果是多级代理，取第一个IP
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }

  /**
   * 从响应中的 access_token 提取用户名（用于 authorization_code 模式）
   */
  private String extractUsernameFromTokenResponse(String responseBody) {
    if (!StringUtils.hasText(responseBody)) {
      return null;
    }
    try {
      JSONObject json = JSON.parseObject(responseBody);
      String accessToken = json.getString("access_token");
      if (StringUtils.hasText(accessToken)) {
        return extractUsernameFromJwtToken(accessToken);
      }
    } catch (Exception e) {
      log.debug("Failed to extract username from token response", e);
    }
    return null;
  }

  /**
   * 从 JWT token 中提取用户名
   */
  private String extractUsernameFromJwtToken(String token) {
    if (!StringUtils.hasText(token)) {
      return null;
    }
    try {
      String signingKey = oauthConfig.getSigningKey();
      JwtParser parser = Jwts.parser().setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8));
      Jws<Claims> claims = parser.parseClaimsJws(token);
      Claims body = claims.getBody();
      // 优先从 user_name 字段获取
      String username = (String) body.get("user_name");
      if (username == null) {
        // 如果没有 user_name，从 subject 获取
        username = body.getSubject();
      }
      return username;
    } catch (Exception e) {
      log.debug("Failed to extract username from JWT token", e);
      return null;
    }
  }

  /**
   * 记录登录日志
   */
  private void recordLoginLog(String username, String loginIp, String userAgent,
                              int httpStatus, String responseBody, String grantType, String clientId) {
    try {
      SysLoginLog loginLog = new SysLoginLog();
      loginLog.setUsername(username);
      loginLog.setLoginIp(loginIp);
      loginLog.setUserAgent(userAgent);
      loginLog.setLoginTime(LocalDateTime.now());
      loginLog.setTenantId(TenantContext.getTenantId());
      loginLog.setClientId(clientId);

      // 判断登录是否成功
      boolean isSuccess = httpStatus == 200 && isSuccessResponse(responseBody);
      loginLog.setLoginStatus(isSuccess ? "SUCCESS" : "FAILURE");

      // 获取用户ID（如果登录成功）
      if (isSuccess && StringUtils.hasText(username)) {
        try {
          SysUser user = userService.findByUsername(username);
          if (user != null) {
            loginLog.setUserId(user.getId());
            // 成功登录后，强制以用户归属租户为准，避免域名上下文导致错租户日志。
            loginLog.setTenantId(user.getTenantId());
          }
        } catch (Exception e) {
          log.warn("Failed to get user ID for username: {}", username, e);
        }
      } else if (isSuccess && !StringUtils.hasText(username)) {
        // 如果登录成功但没有用户名，尝试从响应中的 token 再次提取
        username = extractUsernameFromTokenResponse(responseBody);
        if (StringUtils.hasText(username)) {
          loginLog.setUsername(username);
          try {
            SysUser user = userService.findByUsername(username);
            if (user != null) {
              loginLog.setUserId(user.getId());
              // 成功登录后，强制以用户归属租户为准。
              loginLog.setTenantId(user.getTenantId());
            }
          } catch (Exception e) {
            log.warn("Failed to get user ID for username: {}", username, e);
          }
        }
      }

      // 设置登录消息
      if (!isSuccess) {
        String errorMessage = extractErrorMessage(responseBody);
        loginLog.setLoginMessage(errorMessage != null ? errorMessage : "登录失败");
      } else {
        loginLog.setLoginMessage("登录成功");
      }

      // 保存登录日志
      loginLogService.create(loginLog);
      log.debug("Login log recorded: username={}, status={}", username, loginLog.getLoginStatus());

    } catch (Exception e) {
      log.error("Failed to record login log for username: {}", username, e);
    }
  }

  /**
   * 判断响应是否表示登录成功
   */
  private boolean isSuccessResponse(String responseBody) {
    if (!StringUtils.hasText(responseBody)) {
      return false;
    }
    try {
      JSONObject json = JSON.parseObject(responseBody);
      // OAuth2成功响应包含access_token
      return json.containsKey("access_token");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 从响应中提取错误信息
   */
  private String extractErrorMessage(String responseBody) {
    if (!StringUtils.hasText(responseBody)) {
      return null;
    }
    try {
      JSONObject json = JSON.parseObject(responseBody);
      String error = json.getString("error");
      String errorDescription = json.getString("error_description");
      if (StringUtils.hasText(errorDescription)) {
        return errorDescription;
      }
      if (StringUtils.hasText(error)) {
        return error;
      }
    } catch (Exception e) {
      // 忽略解析错误
    }
    return null;
  }
}


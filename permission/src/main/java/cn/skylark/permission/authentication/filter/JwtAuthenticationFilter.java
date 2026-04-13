package cn.skylark.permission.authentication.filter;

import cn.skylark.permission.authentication.OauthConfig;
import cn.skylark.permission.authentication.service.CustomUserDetailsServiceImpl;
import cn.skylark.permission.authentication.utils.AuthRetWriter;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.nio.charset.StandardCharsets;

/**
 * JWT Filter
 * checking JWT token of httpServletRequest
 *
 * @author yaomianwei
 * @since 2025/11/6
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String TOKEN_USER_NAME_KEY = "user_name";

  @Resource
  private OauthConfig oauthConfig;
  @Resource
  private CustomUserDetailsServiceImpl userDetailsService;

  /**
   * Get JWT token from HttpServletRequest
   */
  private String extractToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
      return authorization.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  /**
   * set authentication into context
   *
   * @param token user token
   */
  private void setAuthentication(String token) {
    String signingKey = oauthConfig.getSigningKey();
    JwtParser parser = Jwts.parser().setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8));
    Jws<Claims> claims = parser.parseClaimsJws(token);
    Claims body = claims.getBody();
    String username = (String) body.get(TOKEN_USER_NAME_KEY);
    if (username == null) {
      username = body.getSubject();
    }
    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    if (username != null && existingAuth == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      Authentication authentication = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } else {
      log.warn("Skipping authentication setup - username: {}, existingAuth: {}",
              username, existingAuth != null ? "present" : "null");
    }
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String token = extractToken(request);
    if (token != null) {
      try {
        setAuthentication(token);
      } catch (Exception e) {
        log.error("JWT token validation failed for request {}", request.getRequestURI(), e);
        if (e instanceof ExpiredJwtException) {
          AuthRetWriter.retExpiredJwtUnauthorized(response);
        } else {
          AuthRetWriter.retCheckJwtException(response);
        }
        return;
      }
    } else {
      log.warn("No JWT token found in request: {}", request.getRequestURI());
      log.warn("Authorization header: {}", request.getHeader("Authorization"));
    }

    filterChain.doFilter(request, response);
  }


}


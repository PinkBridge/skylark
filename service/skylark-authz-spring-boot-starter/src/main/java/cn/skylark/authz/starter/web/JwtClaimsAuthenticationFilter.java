package cn.skylark.authz.starter.web;

import cn.skylark.authz.starter.SkylarkAuthzProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Parses permission-issued JWT access token and populates Spring Security context.
 * Also stores JWT claims into Authentication.details so other components can read them.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtClaimsAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final SkylarkAuthzProperties props;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!props.isEnabled() || props.getJwt() == null || !props.getJwt().isEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication existing = SecurityContextHolder.getContext().getAuthentication();
    if (existing != null && existing.isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = extractToken(request);
    if (!StringUtils.hasText(token)) {
      filterChain.doFilter(request, response);
      return;
    }

    String signingKey = props.getJwt().getSigningKey();
    if (!StringUtils.hasText(signingKey)) {
      log.warn("skylark.authz.jwt.enabled=true but skylark.authz.jwt.signing-key is empty");
      filterChain.doFilter(request, response);
      return;
    }

    try {
      JwtParser parser = Jwts.parser().setSigningKey(signingKey.getBytes(StandardCharsets.UTF_8));
      Jws<Claims> parsed = parser.parseClaimsJws(token);
      Claims claims = parsed.getBody();

      String username = claims.get("user_name", String.class);
      if (username == null) {
        username = claims.getSubject();
      }
      String clientId = claims.get("client_id", String.class);

      Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);
      Object principal = StringUtils.hasText(username) ? username : (StringUtils.hasText(clientId) ? clientId : "anonymous");

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
      // Copy claims into details as a Map<String,Object> for downstream usage
      auth.setDetails(claimsToMap(claims));
      SecurityContextHolder.getContext().setAuthentication(auth);

      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      writeUnauthorized(response, 401, "expired.jwt.unauthorized");
    } catch (Exception e) {
      log.warn("JWT validation failed for {}", request.getRequestURI(), e);
      writeUnauthorized(response, 401, "check.jwt.exception");
    }
  }

  private static String extractToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
      return authorization.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  private static Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
    Object authorities = claims.get("authorities");
    if (authorities instanceof List) {
      List<?> list = (List<?>) authorities;
      List<GrantedAuthority> out = new ArrayList<>();
      for (Object a : list) {
        if (a == null) continue;
        out.add(new SimpleGrantedAuthority(String.valueOf(a)));
      }
      return out;
    }
    Object scope = claims.get("scope");
    if (scope != null) {
      return Collections.singletonList(new SimpleGrantedAuthority("SCOPE_" + scope));
    }
    Object clientId = claims.get("client_id");
    if (clientId != null) {
      return Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE"));
    }
    return Collections.emptyList();
  }

  private static Map<String, Object> claimsToMap(Claims claims) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (claims == null) {
      return out;
    }
    for (Map.Entry<String, Object> e : claims.entrySet()) {
      if (e.getKey() != null) {
        out.put(e.getKey(), e.getValue());
      }
    }
    return out;
  }

  private static void writeUnauthorized(HttpServletResponse response, int code, String message) throws IOException {
    response.setStatus(code);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"code\":" + code + ",\"data\":null,\"message\":\"" + message + "\"}");
  }
}


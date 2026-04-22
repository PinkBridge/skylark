package cn.skylark.datadomain.starter.resolve;

import cn.skylark.datadomain.starter.SkylarkDataDomainProperties;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO;
import cn.skylark.datadomain.starter.permission.ServiceTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calls permission service to resolve data scope, with a small in-memory TTL cache.
 * <p>
 * Permission must expose {@code dataScopeResolvePath} returning JSON compatible with
 * {@link ResolvedDataScopeDTO} (including optional {@code userId} for self-only row scope).
 */
@Slf4j
@RequiredArgsConstructor
public class CachedPermissionRemoteDataScopeResolver implements DataScopeResolver {

  private final RestTemplate restTemplate;
  private final SkylarkDataDomainProperties props;
  private final ServiceTokenProvider tokenProvider;
  private final Clock clock;

  private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

  @Override
  public ResolvedDataScopeDTO resolve(Authentication authentication) {
    String username = extractUsername(authentication);
    if (!StringUtils.hasText(username)) {
      return new ResolvedDataScopeDTO();
    }
    Long tenantId = DataDomainContext.getTenantId();
    String key = String.valueOf(tenantId) + ":" + username;
    long ttl = Math.max(0L, props.getResolveCacheTtlMs());
    Instant now = clock.instant();
    if (ttl > 0) {
      CacheEntry e = cache.get(key);
      if (e != null && e.getExpiresAt().isAfter(now)) {
        return e.getDto();
      }
    }
    ResolvedDataScopeDTO dto = fetchRemote(username, tenantId);
    if (ttl > 0) {
      cache.put(key, new CacheEntry(dto, now.plusMillis(ttl)));
    }
    return dto;
  }

  private ResolvedDataScopeDTO fetchRemote(String username, Long tenantId) {
    UriComponentsBuilder ub = UriComponentsBuilder
            .fromHttpUrl(props.getPermissionBaseUrl() + props.getDataScopeResolvePath())
            .queryParam("username", username);
    if (tenantId != null) {
      ub.queryParam("tenantId", tenantId);
    }
    String url = ub.toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(tokenProvider.getAccessToken());
    HttpEntity<Void> req = new HttpEntity<>(headers);
    try {
      ResponseEntity<ResolvedDataScopeDTO> resp =
              restTemplate.exchange(url, HttpMethod.GET, req, ResolvedDataScopeDTO.class);
      if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
        log.warn("Data scope resolve failed: {}", resp.getStatusCode());
        return new ResolvedDataScopeDTO();
      }
      return resp.getBody();
    } catch (Exception ex) {
      log.warn("Data scope resolve error: {}", ex.getMessage());
      log.debug("Data scope resolve exception", ex);
      return new ResolvedDataScopeDTO();
    }
  }

  private static String extractUsername(Authentication authentication) {
    Object p = authentication.getPrincipal();
    if (p instanceof UserDetails) {
      return ((UserDetails) p).getUsername();
    }
    if (p instanceof String) {
      return (String) p;
    }
    return authentication.getName();
  }

  @Value
  private static class CacheEntry {
    ResolvedDataScopeDTO dto;
    Instant expiresAt;
  }
}

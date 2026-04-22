package cn.skylark.datadomain.starter.permission;

import cn.skylark.datadomain.starter.SkylarkDataDomainProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public class ServiceTokenProvider {

  private static final Duration EARLY_REFRESH = Duration.ofSeconds(15);

  private final RestTemplate restTemplate;
  private final SkylarkDataDomainProperties props;
  private final Clock clock;
  private final AtomicReference<Token> cached = new AtomicReference<>();

  public String getAccessToken() {
    Token t = cached.get();
    Instant now = clock.instant();
    if (t != null && t.getExpiresAt().isAfter(now.plus(EARLY_REFRESH))) {
      return t.getAccessToken();
    }
    synchronized (this) {
      t = cached.get();
      now = clock.instant();
      if (t != null && t.getExpiresAt().isAfter(now.plus(EARLY_REFRESH))) {
        return t.getAccessToken();
      }
      Token fresh = fetchToken();
      cached.set(fresh);
      return fresh.getAccessToken();
    }
  }

  private Token fetchToken() {
    String url = props.getPermissionBaseUrl() + props.getTokenPath();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(props.getClientId(), props.getClientSecret());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "client_credentials");
    HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
    ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, req, Map.class);
    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("Failed to fetch service token: " + resp.getStatusCode());
    }
    Map body = resp.getBody();
    Object token = body.get("access_token");
    Object expiresIn = body.get("expires_in");
    if (!(token instanceof String)) {
      throw new IllegalStateException("token response missing access_token");
    }
    long seconds = 300L;
    if (expiresIn instanceof Number) {
      seconds = ((Number) expiresIn).longValue();
    }
    Instant expiresAt = clock.instant().plusSeconds(Math.max(1, seconds));
    log.debug("Fetched service token for datadomain, expires in {}s", seconds);
    return new Token((String) token, expiresAt);
  }

  @Value
  private static class Token {
    String accessToken;
    Instant expiresAt;
  }
}

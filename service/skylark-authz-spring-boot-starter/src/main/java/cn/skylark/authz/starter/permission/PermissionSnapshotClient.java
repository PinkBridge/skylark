package cn.skylark.authz.starter.permission;

import cn.skylark.authz.starter.SkylarkAuthzProperties;
import cn.skylark.authz.starter.model.RbacSnapshotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client used by business services to sync RBAC snapshot from permission service.
 */
@RequiredArgsConstructor
public class PermissionSnapshotClient {
  private final RestTemplate restTemplate;
  private final SkylarkAuthzProperties props;
  private final ServiceTokenProvider tokenProvider;

  public RbacSnapshotResponse fetchRbacSnapshot(long sinceVersion) {
    String url = UriComponentsBuilder
            .fromHttpUrl(props.getPermissionBaseUrl() + props.getSnapshotPath())
            .queryParam("appCode", props.getAppCode())
            .queryParam("sinceVersion", sinceVersion)
            .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(tokenProvider.getAccessToken());
    HttpEntity<Void> req = new HttpEntity<>(headers);

    ResponseEntity<RbacSnapshotResponse> resp = restTemplate.exchange(url, HttpMethod.GET, req, RbacSnapshotResponse.class);
    if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
      throw new IllegalStateException("Failed to fetch RBAC snapshot from permission: " + resp.getStatusCode());
    }
    return resp.getBody();
  }
}


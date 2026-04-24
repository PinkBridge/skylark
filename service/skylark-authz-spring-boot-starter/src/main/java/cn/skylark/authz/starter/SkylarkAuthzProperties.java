package cn.skylark.authz.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "skylark.authz")
@Data
public class SkylarkAuthzProperties {

  /**
   * Enable/disable starter enforcement.
   */
  private boolean enabled = true;

  /**
   * Logical app code used to fetch RBAC snapshot from permission service.
   */
  private String appCode;

  /**
   * Base URL of permission service, e.g. http://permission:8080
   */
  private String permissionBaseUrl;

  /**
   * OAuth2 token endpoint path on permission service.
   */
  private String tokenPath = "/oauth/token";

  /**
   * RBAC snapshot endpoint path on permission service.
   * Expected to return {@link cn.skylark.authz.starter.model.RbacSnapshotResponse}.
   */
  private String snapshotPath = "/api/permission/authz/snapshot";

  /**
   * Optional: gateway/public whitelist snapshot endpoint if you want to reuse in services.
   */
  private String whitelistSnapshotPath = "/api/permission/gateway/whitelist/snapshot";

  /**
   * OAuth2 client credentials for service-to-permission sync.
   */
  private String clientId;
  private String clientSecret;

  /**
   * Fixed delay between snapshot sync runs.
   */
  private long syncFixedDelayMs = 30_000L;

  /**
   * Paths that bypass authz enforcement (ant patterns).
   */
  private List<String> ignorePaths = new ArrayList<>();

  /**
   * If true, unauthenticated requests (not in ignorePaths) are rejected with 401.
   */
  private boolean requireAuthentication = true;

  /**
   * JWT authentication (parses JWT into Spring Security context).
   * <p>
   * When enabled, the starter will:
   * - validate Authorization: Bearer token using {@link JwtProperties#signingKey}
   * - set Authentication into SecurityContextHolder if absent
   * - copy JWT payload (claims) into Authentication.details as a Map so other starters (e.g. datadomain) can read it
   */
  private JwtProperties jwt = new JwtProperties();

  @Data
  public static class JwtProperties {
    /**
     * Enable JWT parsing filter. Default false to avoid changing security behavior unexpectedly.
     */
    private boolean enabled = false;

    /**
     * Signing key used by permission service to sign JWT (same as permission `oauth.signingKey`).
     */
    private String signingKey;
  }
}


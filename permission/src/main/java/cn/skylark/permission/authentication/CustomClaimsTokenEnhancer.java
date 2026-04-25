package cn.skylark.permission.authentication;

import cn.skylark.permission.authorization.dto.ResolvedDataScopeDTO;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.service.DataDomainResolutionService;
import cn.skylark.permission.authorization.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds business context claims into JWT access token:
 * tenant_id, org_id, user_id, org_ids.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class CustomClaimsTokenEnhancer implements TokenEnhancer {

  public static final String CLAIM_TENANT_ID = "tenant_id";
  public static final String CLAIM_ORG_ID = "org_id";
  public static final String CLAIM_USER_ID = "user_id";
  public static final String CLAIM_ORG_IDS = "org_ids";
  /** Data-scope flags for downstream services when remote resolve-data-scope is disabled. */
  public static final String CLAIM_DATA_SCOPE_ALL_PLATFORM = "data_scope_all_platform";
  public static final String CLAIM_DATA_SCOPE_WHOLE_TENANT = "data_scope_whole_tenant";
  public static final String CLAIM_DATA_SCOPE_SELF_ONLY = "data_scope_self_only";

  private final UserService userService;
  private final DataDomainResolutionService dataDomainResolutionService;

  @Override
  public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
    if (!(accessToken instanceof DefaultOAuth2AccessToken)) {
      return accessToken;
    }
    DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;

    String username = extractUsername(authentication);
    if (!StringUtils.hasText(username)) {
      // client_credentials: no user context
      return token;
    }

    SysUser user = userService.findByUsername(username);
    if (user == null) {
      return token;
    }

    List<SysRole> roles = userService.roles(user.getId());
    ResolvedDataScopeDTO scope = dataDomainResolutionService.resolve(user, roles);

    Map<String, Object> info = new HashMap<>();
    if (user.getTenantId() != null) {
      info.put(CLAIM_TENANT_ID, user.getTenantId());
    }
    if (user.getOrgId() != null) {
      info.put(CLAIM_ORG_ID, user.getOrgId());
    }
    if (user.getId() != null) {
      info.put(CLAIM_USER_ID, user.getId());
    }
    if (scope != null && scope.getOrgIds() != null) {
      info.put(CLAIM_ORG_IDS, scope.getOrgIds());
    }
    if (scope != null) {
      info.put(CLAIM_DATA_SCOPE_ALL_PLATFORM, scope.isAllPlatform());
      info.put(CLAIM_DATA_SCOPE_WHOLE_TENANT, scope.isWholeTenant());
      info.put(CLAIM_DATA_SCOPE_SELF_ONLY, scope.isSelfOnly());
    }

    // DefaultTokenServices may create DefaultOAuth2AccessToken with an unmodifiable additionalInformation map.
    // Always replace with a merged mutable map.
    Map<String, Object> merged = new HashMap<>();
    Map<String, Object> existing = token.getAdditionalInformation();
    if (existing != null && !existing.isEmpty()) {
      merged.putAll(existing);
    }
    merged.putAll(info);
    token.setAdditionalInformation(merged);
    return token;
  }

  private static String extractUsername(OAuth2Authentication authentication) {
    if (authentication == null) {
      return null;
    }
    Authentication userAuth = authentication.getUserAuthentication();
    if (userAuth == null) {
      return null;
    }
    Object p = userAuth.getPrincipal();
    if (p instanceof org.springframework.security.core.userdetails.UserDetails) {
      return ((org.springframework.security.core.userdetails.UserDetails) p).getUsername();
    }
    if (p instanceof String) {
      return (String) p;
    }
    return userAuth.getName();
  }
}


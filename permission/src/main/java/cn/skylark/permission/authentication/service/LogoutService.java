package cn.skylark.permission.authentication.service;

import cn.skylark.permission.authentication.OauthConfig;
import cn.skylark.permission.authentication.mapper.RefreshTokenMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
@SuppressWarnings("deprecation")
public class LogoutService {
  @Resource
  private RefreshTokenMapper refreshTokenMapper;
  @Resource
  private OauthConfig oauthConfig;

  /**
   * logout: delete refresh token
   * extract information from JWT access token, then delete the corresponding refresh token
   *
   * @param accessToken JWT access token
   * @return whether the refresh token is deleted successfully
   */
  public boolean logout(String accessToken) {
    if (accessToken == null || accessToken.isEmpty()) {
      return false;
    }

    Claims claims;
    try {
      claims = Jwts.parser().setSigningKey(oauthConfig.getSigningKey())
              .parseClaimsJws(accessToken).getBody();
    }catch (Exception e) {
      return false;
    }

    String jti = claims.getId();
    if (tryDeleteRefreshToken(jti)) {
      return true;
    }

    String ati = claims.get("ati", String.class);
    return tryDeleteRefreshToken(ati);
  }

  /**
   * try to delete refresh token by token id
   *
   * @param tokenId token ID
   * @return
   */
  private boolean tryDeleteRefreshToken(String tokenId) {
    if (tokenId == null || tokenId.isEmpty()) {
      return false;
    }
    int deleted = refreshTokenMapper.deleteByTokenId(tokenId);
    return deleted > 0;
  }
}

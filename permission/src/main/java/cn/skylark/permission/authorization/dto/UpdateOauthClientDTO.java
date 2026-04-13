package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 更新OAuth2客户端信息DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UpdateOauthClientDTO {
  /**
   * 资源ID列表
   */
  private String resourceIds;

  /**
   * 客户端密钥（可选，如果为空则不更新）
   */
  private String clientSecret;

  /**
   * 作用域
   */
  private String scope;

  /**
   * 授权模式
   */
  private String authorizedGrantTypes;

  /**
   * 重定向URI
   */
  private String webServerRedirectUri;

  /**
   * 权限
   */
  private String authorities;

  /**
   * 访问令牌有效期（秒）
   */
  private Integer accessTokenValidity;

  /**
   * 刷新令牌有效期（秒）
   */
  private Integer refreshTokenValidity;

  /**
   * 附加信息
   */
  private String additionalInformation;

  /**
   * 自动授权
   */
  private String autoapprove;
}


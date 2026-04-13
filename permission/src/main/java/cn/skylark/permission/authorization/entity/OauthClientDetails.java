package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OAuth2客户端详情实体
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class OauthClientDetails {
  /**
   * 客户端ID（主键）
   */
  private String clientId;

  /**
   * 资源ID列表
   */
  private String resourceIds;

  /**
   * 客户端密钥
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

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}


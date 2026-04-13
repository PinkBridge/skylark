package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OAuth2客户端响应DTO（不包含密钥）
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class OauthClientResponseDTO {
  /**
   * 客户端ID
   */
  private String clientId;

  /**
   * 资源ID列表
   */
  private String resourceIds;

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


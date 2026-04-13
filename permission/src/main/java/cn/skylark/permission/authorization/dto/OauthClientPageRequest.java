package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OAuth2客户端分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OauthClientPageRequest extends PageRequest {
  /**
   * 客户端ID（模糊搜索）
   */
  private String clientId;

  /**
   * 授权模式（模糊搜索）
   */
  private String authorizedGrantTypes;

  /**
   * 作用域（模糊搜索）
   */
  private String scope;
}


package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogPageRequest extends PageRequest {
  /**
   * 用户名（模糊搜索）
   */
  private String username;

  /**
   * 登录IP（模糊搜索）
   */
  private String loginIp;

  /**
   * 登录状态：SUCCESS-成功，FAILURE-失败
   */
  private String loginStatus;

  /**
   * 登录时间开始（查询此时间之后的数据）
   */
  private LocalDateTime loginTimeStart;

  /**
   * 登录时间结束（查询此时间之前的数据）
   */
  private LocalDateTime loginTimeEnd;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 应用ID（OAuth2客户端ID，模糊搜索）
   */
  private String clientId;
}


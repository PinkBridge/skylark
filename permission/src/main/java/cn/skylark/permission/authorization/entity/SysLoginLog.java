package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Data
public class SysLoginLog {
  /**
   * 主键ID
   */
  private Long id;

  /**
   * 用户ID
   */
  private Long userId;

  /**
   * 用户名
   */
  private String username;

  /**
   * 登录IP
   */
  private String loginIp;

  /**
   * 登录地点
   */
  private String loginLocation;

  /**
   * 用户代理（浏览器信息）
   */
  private String userAgent;

  /**
   * 登录状态：SUCCESS-成功，FAILURE-失败
   */
  private String loginStatus;

  /**
   * 登录消息（失败原因等）
   */
  private String loginMessage;

  /**
   * 登录时间
   */
  private LocalDateTime loginTime;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 应用ID（OAuth2客户端ID）
   */
  private String clientId;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}


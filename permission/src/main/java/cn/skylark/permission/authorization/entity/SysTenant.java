package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统租户实体
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class SysTenant {

  /**
   * 租户ID
   */
  private Long id;

  /**
   * 租户名称
   */
  private String name;

  /**
   * 系统名称
   */
  private String systemName;

  /**
   * 租户编码（唯一）
   */
  private String code;

  /**
   * 联系人姓名
   */
  private String contactName;

  /**
   * 联系人电话
   */
  private String contactPhone;

  /**
   * 联系人邮箱
   */
  private String contactEmail;

  /**
   * 租户访问域名
   */
  private String domain;

  /**
   * 租户Logo地址
   */
  private String logo;

  /**
   * 租户地址
   */
  private String address;

  /**
   * 租户描述
   */
  private String description;

  /**
   * 状态：ACTIVE-启用，INACTIVE-禁用
   */
  private String status;

  /**
   * 租户到期时间
   */
  private LocalDateTime expireTime;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}



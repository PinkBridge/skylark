package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class SysUser {
  /**
   * 用户ID
   */
  private Long id;

  /**
   * 用户名
   */
  private String username;

  /**
   * 密码
   */
  private String password;

  /**
   * 是否启用：1-启用，0-禁用
   */
  private Boolean enabled;

  /**
   * 性别：M-男，F-女
   */
  private String gender;

  /**
   * 照片URL
   */
  private String avatar;

  /**
   * 手机号码
   */
  private String phone;

  /**
   * 电子邮箱
   */
  private String email;

  /**
   * 状态：ACTIVE-活跃，INACTIVE-非活跃，LOCKED-锁定
   */
  private String status;

  /**
   * 省份
   */
  private String province;

  /**
   * 城市
   */
  private String city;

  /**
   * 详细地址
   */
  private String address;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 组织ID
   */
  private Long orgId;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}

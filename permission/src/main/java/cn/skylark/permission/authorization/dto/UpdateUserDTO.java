package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.util.List;

/**
 * 更新用户信息DTO（不包含密码）
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UpdateUserDTO {
  /**
   * 用户名
   */
  private String username;

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
   * 组织ID
   */
  private Long orgId;

  /**
   * 角色ID数组（如果提供，会先删除原有关联，再新增关联）
   */
  private List<Long> roleIds;
}


package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 当前登录用户可自助修改的资料（不包含用户名、角色、组织等敏感字段）。
 */
@Data
public class UpdateMyProfileDTO {

  /** 头像 URL */
  private String avatar;

  private String phone;

  private String email;

  /** 性别：M / F */
  private String gender;

  /** 详细地址 */
  private String address;
}

package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 修改密码DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class ChangePasswordDTO {
  /**
   * 旧密码
   */
  private String oldPassword;

  /**
   * 新密码
   */
  private String newPassword;
}


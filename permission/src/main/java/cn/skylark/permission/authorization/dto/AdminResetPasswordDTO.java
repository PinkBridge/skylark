package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 管理员重置用户密码请求
 */
@Data
public class AdminResetPasswordDTO {
  private String newPassword;
}


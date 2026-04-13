package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 创建租户管理员请求
 */
@Data
public class CreateTenantAdminDTO {
  private String username;
  private String password;
  private Long roleId;
}

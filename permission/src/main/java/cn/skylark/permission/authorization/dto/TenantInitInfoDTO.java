package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * Tenant initialization info: default org + default user + role binding.
 */
@Data
public class TenantInitInfoDTO {
  private OrgInfo org;
  private UserInfo user;
  private RoleInfo role;

  @Data
  public static class OrgInfo {
    private Long id;
    private String name;
    private String type;
  }

  @Data
  public static class UserInfo {
    private Long id;
    private String username;
  }

  @Data
  public static class RoleInfo {
    private Long id;
    private String name;
  }
}


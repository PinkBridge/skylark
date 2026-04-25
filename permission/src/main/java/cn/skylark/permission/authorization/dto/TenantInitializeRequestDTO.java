package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * Tenant initialization request: create default organization + default user.
 */
@Data
public class TenantInitializeRequestDTO {

  private OrgInfo org;
  private UserInfo user;

  @Data
  public static class OrgInfo {
    /** Organization name */
    private String name;
    /** Organization type: COMPANY / DEPARTMENT / TEAM */
    private String type;
  }

  @Data
  public static class UserInfo {
    /** Role id to bind to the default user */
    private Long roleId;
    /** Login username */
    private String username;
    /** Login password */
    private String password;
  }
}


package cn.skylark.permission.authorization.dto;

import lombok.Data;

@Data
public class PlatformInitializeRequestDTO {
  private InitTenantDTO tenant;
  private InitAdminDTO admin;

  @Data
  public static class InitTenantDTO {
    private String name;
    private String systemName;
    private String domain;
    private String logo;
    private String address;
    private String description;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
  }

  @Data
  public static class InitAdminDTO {
    private String username;
    private String password;
    private String phone;
    private String email;
  }
}


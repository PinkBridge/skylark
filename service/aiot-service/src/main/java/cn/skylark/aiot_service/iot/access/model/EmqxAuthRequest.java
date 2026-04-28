package cn.skylark.aiot_service.iot.access.model;

import lombok.Data;

@Data
public class EmqxAuthRequest {
  private String username;
  private String password;
}


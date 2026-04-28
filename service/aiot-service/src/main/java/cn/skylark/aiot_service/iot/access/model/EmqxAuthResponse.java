package cn.skylark.aiot_service.iot.access.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmqxAuthResponse {
  private String result;

  public static EmqxAuthResponse allow() {
    return new EmqxAuthResponse("allow");
  }

  public static EmqxAuthResponse deny() {
    return new EmqxAuthResponse("deny");
  }
}


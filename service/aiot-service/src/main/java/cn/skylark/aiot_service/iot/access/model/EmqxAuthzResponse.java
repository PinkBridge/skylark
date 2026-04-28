package cn.skylark.aiot_service.iot.access.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmqxAuthzResponse {
  private String result;

  public static EmqxAuthzResponse allow() {
    return new EmqxAuthzResponse("allow");
  }

  public static EmqxAuthzResponse deny() {
    return new EmqxAuthzResponse("deny");
  }
}


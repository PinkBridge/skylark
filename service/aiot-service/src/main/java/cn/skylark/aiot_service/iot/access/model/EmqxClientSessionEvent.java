package cn.skylark.aiot_service.iot.access.model;

import lombok.Data;

@Data
public class EmqxClientSessionEvent {
  private String event;
  private String username;
  private String clientid;
  private String peername;
  private String node;
  private String reason;
  private Long timestamp;
}


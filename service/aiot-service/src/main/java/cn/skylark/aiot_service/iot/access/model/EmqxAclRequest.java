package cn.skylark.aiot_service.iot.access.model;

import lombok.Data;

@Data
public class EmqxAclRequest {
  private String username;
  private String action;
  private String topic;
}


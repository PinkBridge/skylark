package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

@Data
public class ProductDataChannelResponse {
  private Long id;
  private String action;
  private String topicPattern;
  private String effect;
  private Integer priority;
  private Boolean enabled;
}


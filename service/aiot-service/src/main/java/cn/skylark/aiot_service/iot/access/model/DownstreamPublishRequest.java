package cn.skylark.aiot_service.iot.access.model;

import lombok.Data;

@Data
public class DownstreamPublishRequest {
  private String traceId;
  private String topic;
  private String payload;
  private Integer qos;
  private Boolean retain;
}


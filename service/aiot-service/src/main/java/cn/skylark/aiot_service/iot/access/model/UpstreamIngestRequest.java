package cn.skylark.aiot_service.iot.access.model;

import lombok.Data;

@Data
public class UpstreamIngestRequest {
  private String traceId;
  private String deviceId;
  private String messageType;
  private String topic;
  private Long timestamp;
  private String payload;
}


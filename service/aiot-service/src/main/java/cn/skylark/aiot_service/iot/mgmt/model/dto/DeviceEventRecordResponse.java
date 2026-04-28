package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceEventRecordResponse {
  private String eventName;
  private String traceId;
  private String messageId;
  private String topic;
  private Long deviceTimestamp;
  private String payload;
  private LocalDateTime createdAt;
}


package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceCurrentPropertyResponse {
  private String propertyIdentifier;
  private String propertyValue;
  private Long deviceTimestamp;
  private LocalDateTime createdAt;
}


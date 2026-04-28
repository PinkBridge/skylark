package cn.skylark.aiot_service.iot.mgmt.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceServiceRecordEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String productKey;
  private String deviceName;
  private String serviceName;
  private String direction;
  private String traceId;
  private String messageId;
  private String topic;
  private Long deviceTimestamp;
  private String payload;
  private String outputTopic;
  private Long outputDeviceTimestamp;
  private String outputPayload;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


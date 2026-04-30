package cn.skylark.aiot_service.iot.alarm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotAlarmEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long ruleId;
  private String deviceGroupKey;
  private String productKey;
  private String deviceKey;
  private String severity;
  private String status;
  private LocalDateTime firstTriggeredAt;
  private LocalDateTime lastTriggeredAt;
  private LocalDateTime recoveredAt;
  private Integer triggerCount;
  private String evidenceJson;
  private String lastEventId;
  private String lastEventType;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


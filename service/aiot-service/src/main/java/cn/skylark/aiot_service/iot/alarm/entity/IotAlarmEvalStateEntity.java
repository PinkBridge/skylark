package cn.skylark.aiot_service.iot.alarm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotAlarmEvalStateEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long ruleId;
  private String deviceGroupKey;
  private String productKey;
  private String deviceKey;
  private LocalDateTime conditionMetSince;
  private LocalDateTime lastSeenAt;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


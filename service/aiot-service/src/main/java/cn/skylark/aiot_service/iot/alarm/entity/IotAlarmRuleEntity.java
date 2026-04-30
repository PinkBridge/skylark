package cn.skylark.aiot_service.iot.alarm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotAlarmRuleEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String deviceGroupKey;
  private String name;
  private String sourceType;
  private String severity;
  private String triggerMode;
  private Integer durationSeconds;
  private String recoveryMode;
  private String dedupMode;
  private Integer enabled;
  private String conditionJson;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


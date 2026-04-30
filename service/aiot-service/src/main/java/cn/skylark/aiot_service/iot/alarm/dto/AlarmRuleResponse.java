package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmRuleResponse {
  private Long id;
  private String deviceGroupKey;
  private String name;
  private String sourceType;
  private String severity;
  private String triggerMode;
  private Integer durationSeconds;
  private String recoveryMode;
  private String dedupMode;
  private Boolean enabled;
  private String conditionJson;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


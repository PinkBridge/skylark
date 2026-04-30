package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AlarmRuleCreateRequest {
  @NotBlank
  private String deviceGroupKey;
  @NotBlank
  private String name;
  @NotBlank
  private String sourceType; // PROPERTY / EVENT
  @NotBlank
  private String severity; // HIGH / MEDIUM / LOW
  @NotBlank
  private String triggerMode; // INSTANT / DURATION
  @NotNull
  private Integer durationSeconds;
  @NotBlank
  private String recoveryMode; // AUTO / MANUAL
  @NotBlank
  private String dedupMode; // SINGLE_ACTIVE / EVERY_TRIGGER
  @NotNull
  private Boolean enabled;
  @NotBlank
  private String conditionJson;
}


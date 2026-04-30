package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AlarmRuleUpdateRequest {
  @NotBlank
  private String deviceGroupKey;
  @NotBlank
  private String name;
  @NotBlank
  private String sourceType;
  @NotBlank
  private String severity;
  @NotBlank
  private String triggerMode;
  @NotNull
  private Integer durationSeconds;
  @NotBlank
  private String recoveryMode;
  @NotBlank
  private String dedupMode;
  @NotNull
  private Boolean enabled;
  @NotBlank
  private String conditionJson;
}


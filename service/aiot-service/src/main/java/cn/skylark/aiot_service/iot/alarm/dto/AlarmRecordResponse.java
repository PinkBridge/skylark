package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmRecordResponse {
  private Long id;
  private Long ruleId;
  /** Alarm rule display name (replaces showing raw rule id in UI). */
  private String ruleName;
  /** Rule recovery mode (AUTO / MANUAL); MANUAL enables UI manual recover for ACTIVE records. */
  private String ruleRecoveryMode;
  /** Device display name from device registry, if any. */
  private String deviceName;
  private String deviceGroupKey;
  private String productKey;
  private String deviceKey;
  private String severity;
  private String status;
  private LocalDateTime firstTriggeredAt;
  private LocalDateTime lastTriggeredAt;
  private LocalDateTime recoveredAt;
  private Integer triggerCount;
  /** Human-readable trigger condition at fire time (parsed from {@link #evidenceJson}). */
  private String triggerCondition;
  /** Reported / matched trigger value (derived from evidence + last event fields). */
  private String triggerValue;
  private String evidenceJson;
  private String lastEventId;
  private String lastEventType;
  private LocalDateTime createdAt;
}

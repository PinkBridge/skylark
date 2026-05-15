package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmNotifyConfigResponse {
  private Long id;
  private Long ruleId;
  private String ruleName;
  private String name;
  private String remark;
  private Boolean enabled;
  private Boolean emailEnabled;
  private Boolean smsEnabled;
  private Long emailNotifyChannelId;
  private Long smsNotifyChannelId;
  private String emailChannelName;
  private String smsChannelName;
  private String toEmails;
  private String toMobiles;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

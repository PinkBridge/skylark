package cn.skylark.aiot_service.iot.alarm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotAlarmNotifyConfigEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long ruleId;
  private String name;
  private String remark;
  private Integer enabled;
  private Integer emailEnabled;
  private Integer smsEnabled;
  private Long emailNotifyChannelId;
  private Long smsNotifyChannelId;
  private String toEmails;
  private String toMobiles;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

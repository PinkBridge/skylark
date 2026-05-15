package cn.skylark.aiot_service.iot.alarm.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotAlarmNotifyDeliveryEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long notifyConfigId;
  private Long ruleId;
  private String alarmEventId;
  private String alarmEventType;
  private String channel;
  private String status;
  private String vendorCode;
  private Integer httpStatus;
  private String errorMessage;
  private String recipientHint;
  private Integer isTest;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

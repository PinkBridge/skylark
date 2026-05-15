package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmNotifyDeliveryResponse {
  private Long id;
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
  private Boolean test;
  private LocalDateTime createdAt;
}

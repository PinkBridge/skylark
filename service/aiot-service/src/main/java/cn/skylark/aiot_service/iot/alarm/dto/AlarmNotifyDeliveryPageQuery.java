package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

@Data
public class AlarmNotifyDeliveryPageQuery {
  private Integer pageNum;
  private Integer pageSize;
  private Long ruleId;
  private Long notifyConfigId;
  private String channel;
  private String status;
}

package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

@Data
public class AlarmNotifyConfigPageQuery {
  private Integer pageNum;
  private Integer pageSize;
  private Long ruleId;
}

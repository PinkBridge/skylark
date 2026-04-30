package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

@Data
public class AlarmRulePageQuery {
  private String deviceGroupKey;
  private Boolean enabled;
  private Integer pageNum;
  private Integer pageSize;
}


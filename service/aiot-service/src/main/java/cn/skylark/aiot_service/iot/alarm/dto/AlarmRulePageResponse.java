package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlarmRulePageResponse {
  private Integer pageNum;
  private Integer pageSize;
  private Long total;
  private List<AlarmRuleResponse> records;
}


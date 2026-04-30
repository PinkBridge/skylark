package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

@Data
public class AlarmRecordPageQuery {
  private String deviceGroupKey;
  private Long ruleId;
  private String severity;
  private String status;
  private Integer pageNum;
  private Integer pageSize;
}


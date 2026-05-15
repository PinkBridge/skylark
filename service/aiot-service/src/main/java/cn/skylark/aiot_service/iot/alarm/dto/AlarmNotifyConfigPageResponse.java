package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlarmNotifyConfigPageResponse {
  private Integer pageNum;
  private Integer pageSize;
  private Long total;
  private List<AlarmNotifyConfigResponse> records;
}

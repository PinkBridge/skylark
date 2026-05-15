package cn.skylark.aiot_service.iot.notify.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotifyChannelPageResponse {
  private Integer pageNum;
  private Integer pageSize;
  private Long total;
  private List<NotifyChannelResponse> records;
}

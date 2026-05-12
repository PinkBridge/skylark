package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationChannelPageResponse {
  private Integer pageNum;
  private Integer pageSize;
  private Long total;
  private List<NotificationChannelResponse> records;
}


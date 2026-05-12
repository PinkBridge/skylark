package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationSubscriptionPageResponse {
  private Integer pageNum;
  private Integer pageSize;
  private Long total;
  private List<NotificationSubscriptionResponse> records;
}


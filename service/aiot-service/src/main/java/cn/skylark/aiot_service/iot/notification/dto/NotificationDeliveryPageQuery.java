package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

@Data
public class NotificationDeliveryPageQuery {
  private String status;
  private Long channelId;
  private Long subscriptionId;
  private Integer pageNum;
  private Integer pageSize;
}


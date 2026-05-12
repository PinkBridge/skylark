package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

@Data
public class NotificationSubscriptionPageQuery {
  private Long channelId;
  private String deviceGroupKey;
  private Integer pageNum;
  private Integer pageSize;
}


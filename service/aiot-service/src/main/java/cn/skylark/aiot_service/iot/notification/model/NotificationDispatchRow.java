package cn.skylark.aiot_service.iot.notification.model;

import lombok.Data;

@Data
public class NotificationDispatchRow {
  private Long subscriptionId;
  private Long subscriptionOrgId;
  private Long channelId;
  private String channelType;
  private String channelConfigJson;
  private String deviceGroupKey;
  private String eventTypes;
  private String filterJson;
  private String templateJson;
}


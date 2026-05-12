package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationSubscriptionResponse {
  private Long id;
  private Long channelId;
  private String name;
  private Boolean enabled;
  private String deviceGroupKey;
  private List<String> eventTypes;
  private String filterJson;
  private String templateJson;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


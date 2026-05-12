package cn.skylark.aiot_service.iot.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotNotificationSubscriptionEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long channelId;
  private String name;
  private Integer enabled;
  private String deviceGroupKey;
  private String eventTypes;
  private String filterJson;
  private String templateJson;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


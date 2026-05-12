package cn.skylark.aiot_service.iot.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotNotificationDeliveryEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String eventId;
  private String eventType;
  private Long subscriptionId;
  private Long channelId;
  private String status;
  private Integer attempts;
  private LocalDateTime nextRetryAt;
  private String lastError;
  private Integer httpStatus;
  private String payloadSnapshot;
  private String renderedTitle;
  private String renderedBody;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


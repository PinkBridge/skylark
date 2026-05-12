package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDeliveryResponse {
  private Long id;
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
  private LocalDateTime createdAt;
}


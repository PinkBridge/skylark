package cn.skylark.aiot_service.appint.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutboundDeliveryResponse {
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
  private LocalDateTime createdAt;
}

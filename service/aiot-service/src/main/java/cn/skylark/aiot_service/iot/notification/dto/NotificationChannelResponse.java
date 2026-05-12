package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationChannelResponse {
  private Long id;
  private String name;
  private String type;
  private Boolean enabled;
  private String configJson;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


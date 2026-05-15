package cn.skylark.aiot_service.iot.notify.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotifyChannelResponse {
  private Long id;
  private String channelKind;
  private String provider;
  private String name;
  private Boolean enabled;
  /** Masked secrets */
  private String configJson;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

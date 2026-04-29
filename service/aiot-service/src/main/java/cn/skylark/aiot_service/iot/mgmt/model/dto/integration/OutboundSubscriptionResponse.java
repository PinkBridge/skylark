package cn.skylark.aiot_service.iot.mgmt.model.dto.integration;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OutboundSubscriptionResponse {
  private Long id;
  private Long channelId;
  private String name;
  private Boolean enabled;
  private List<String> eventTypes;
  private String filterJson;
  private String transformJson;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

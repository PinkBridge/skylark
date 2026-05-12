package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class NotificationSubscriptionUpdateRequest {
  @NotNull
  private Long channelId;
  private String name;
  @NotNull
  private Boolean enabled;
  @NotBlank
  private String deviceGroupKey;
  @NotNull
  private List<String> eventTypes;
  private String filterJson;
  @NotBlank
  private String templateJson;
}


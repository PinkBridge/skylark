package cn.skylark.aiot_service.iot.appint.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OutboundSubscriptionCreateRequest {
  @NotNull
  private Long channelId;
  private String name;
  @NotNull
  private Boolean enabled;
  @NotEmpty
  private List<String> eventTypes;
  private String filterJson;
  private String transformJson;
}

package cn.skylark.aiot_service.iot.mgmt.model.dto.integration;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OutboundChannelUpdateRequest {
  @NotBlank
  private String name;
  @NotNull
  private Boolean enabled;
  @NotBlank
  private String configJson;
}

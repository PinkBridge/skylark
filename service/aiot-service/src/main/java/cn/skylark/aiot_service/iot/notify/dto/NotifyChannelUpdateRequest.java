package cn.skylark.aiot_service.iot.notify.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class NotifyChannelUpdateRequest {
  @NotBlank
  @Size(max = 128)
  private String name;
  @NotNull
  private Boolean enabled;
  @NotNull
  @Size(max = 65535)
  private String configJson;
}

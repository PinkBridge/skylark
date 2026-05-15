package cn.skylark.aiot_service.iot.notify.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class NotifyChannelCreateRequest {
  @NotBlank
  @Size(max = 16)
  private String channelKind;
  @NotBlank
  @Size(max = 32)
  private String provider;
  @NotBlank
  @Size(max = 128)
  private String name;
  @NotNull
  private Boolean enabled;
  /** JSON string; secrets may be partial on update via merge */
  @NotNull
  @Size(max = 65535)
  private String configJson;
}

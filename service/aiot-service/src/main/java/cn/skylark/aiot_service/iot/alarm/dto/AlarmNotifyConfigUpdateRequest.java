package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AlarmNotifyConfigUpdateRequest {
  @NotBlank
  @Size(max = 128)
  private String name;
  @Size(max = 512)
  private String remark;
  @NotNull
  private Boolean enabled;
  @NotNull
  private Boolean emailEnabled;
  @NotNull
  private Boolean smsEnabled;
  private Long emailNotifyChannelId;
  private Long smsNotifyChannelId;
  private String toEmails;
  private String toMobiles;
}

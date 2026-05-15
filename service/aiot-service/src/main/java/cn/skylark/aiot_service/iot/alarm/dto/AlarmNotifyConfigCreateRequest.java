package cn.skylark.aiot_service.iot.alarm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AlarmNotifyConfigCreateRequest {
  @NotNull
  private Long ruleId;
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
  /** Selected SMTP notify channel when email is enabled */
  private Long emailNotifyChannelId;
  /** Selected SMS notify channel when SMS is enabled */
  private Long smsNotifyChannelId;
  /** Comma or newline separated emails */
  private String toEmails;
  /** Comma or newline separated mobiles */
  private String toMobiles;
}

package cn.skylark.aiot_service.iot.notify.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class NotifyChannelTestRequest {
  @Size(max = 256)
  private String testEmail;
  @Size(max = 32)
  private String testPhone;
}

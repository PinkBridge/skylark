package cn.skylark.aiot_service.iot.appint.dto;

import lombok.Data;

@Data
public class TestWebhookResponse {
  private boolean ok;
  private Integer httpStatus;
  private String error;
}

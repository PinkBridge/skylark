package cn.skylark.aiot_service.iot.mgmt.model.dto.integration;

import lombok.Data;

@Data
public class TestWebhookResponse {
  private boolean ok;
  private Integer httpStatus;
  private String error;
}

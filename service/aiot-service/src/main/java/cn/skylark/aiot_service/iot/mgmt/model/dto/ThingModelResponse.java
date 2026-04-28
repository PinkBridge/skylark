package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

@Data
public class ThingModelResponse {
  private String productKey;
  private String version;
  private String modelJson;
}


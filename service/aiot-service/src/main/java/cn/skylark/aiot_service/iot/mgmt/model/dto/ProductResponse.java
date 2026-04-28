package cn.skylark.aiot_service.iot.mgmt.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class ProductResponse {
  private String productKey;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String productSecret;

  private String name;
  private String coverImageUrl;
  private String thumbnailUrl;
  private String description;
  private String protocolType;
  private String deviceType;
  private String status;
  private Long deviceCount;
}


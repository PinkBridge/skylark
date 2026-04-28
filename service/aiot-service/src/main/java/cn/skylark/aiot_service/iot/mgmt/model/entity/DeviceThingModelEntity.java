package cn.skylark.aiot_service.iot.mgmt.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceThingModelEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String productKey;
  private String deviceKey;
  private String modelJson;
  private String version;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


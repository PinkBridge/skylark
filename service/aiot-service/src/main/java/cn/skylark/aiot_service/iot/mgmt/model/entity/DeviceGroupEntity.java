package cn.skylark.aiot_service.iot.mgmt.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceGroupEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String groupKey;
  private String name;
  private String description;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


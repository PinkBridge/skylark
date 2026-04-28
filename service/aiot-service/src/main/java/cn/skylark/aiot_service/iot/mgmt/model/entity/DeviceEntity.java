package cn.skylark.aiot_service.iot.mgmt.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String productKey;
  private String deviceName;
  private String address;
  private String deviceKey;
  private String deviceType;
  private String secret;
  private String status;
  private String connectStatus;
  private LocalDateTime lastConnectedAt;
  private LocalDateTime lastDisconnectedAt;
  private String protocolType;
  private String protocolVersion;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


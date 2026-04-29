package cn.skylark.aiot_service.iot.mgmt.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotOutboundChannelEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String type;
  private Integer enabled;
  private String configJson;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

package cn.skylark.aiot_service.iot.mgmt.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotOutboundSubscriptionEntity {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long channelId;
  private String name;
  private Integer enabled;
  private String eventTypes;
  private String filterJson;
  private String transformJson;
  private Integer isDelete;
  private String createUser;
  private String updateUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

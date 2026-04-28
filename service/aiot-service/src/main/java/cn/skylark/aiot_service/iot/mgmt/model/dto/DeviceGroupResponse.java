package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceGroupResponse {
  private String groupKey;
  private String name;
  private String description;
  private long deviceCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}


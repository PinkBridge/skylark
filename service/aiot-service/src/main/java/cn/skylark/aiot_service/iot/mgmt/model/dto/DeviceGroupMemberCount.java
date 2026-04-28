package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

@Data
public class DeviceGroupMemberCount {
  private String groupKey;
  private long memberCount;
}


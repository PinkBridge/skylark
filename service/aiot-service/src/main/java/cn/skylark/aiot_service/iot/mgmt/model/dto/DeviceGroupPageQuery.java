package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.Data;

@Data
public class DeviceGroupPageQuery {
  private String groupKey;
  private String name;
  private Integer pageNum = 1;
  private Integer pageSize = 10;
}


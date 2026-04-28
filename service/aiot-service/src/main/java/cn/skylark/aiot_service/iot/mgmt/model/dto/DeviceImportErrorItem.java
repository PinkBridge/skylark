package cn.skylark.aiot_service.iot.mgmt.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceImportErrorItem {
  private int rowNumber;
  private String message;
}


package cn.skylark.permission.authorization.dto;

import lombok.Data;

@Data
public class PlatformConfigItemDTO {
  private String configKey;
  private String configValue;
  private String valueType;
  private String description;
}

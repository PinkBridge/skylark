package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPlatformConfig {
  private Long id;
  private String configKey;
  private String configValue;
  private String valueType;
  private String description;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}

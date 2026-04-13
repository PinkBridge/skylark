package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysRole {
  private Long id;
  private String name;
  private String remark;
  private Long tenantId;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}

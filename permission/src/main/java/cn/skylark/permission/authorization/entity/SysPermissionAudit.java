package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPermissionAudit {
  private Long id;
  private Long tenantId;
  private String operator;
  private String action;
  private String detail;
  private LocalDateTime createTime;
}


package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户管理员绑定（租户权限上限来源）。
 */
@Data
public class SysTenantAdminBinding {
  private Long tenantId;
  private Long userId;
  private Long roleId;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}


package cn.skylark.permission.authorization.entity;

import lombok.Data;

@Data
public class SysRoleMenu {
  private Long id;
  private Long roleId;
  private Long menuId;
}

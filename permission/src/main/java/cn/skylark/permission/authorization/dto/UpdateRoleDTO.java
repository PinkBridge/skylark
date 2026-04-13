package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 更新角色信息DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UpdateRoleDTO {
  /**
   * 角色名称
   */
  private String name;

  /**
   * 备注说明
   */
  private String remark;
}


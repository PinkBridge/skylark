package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.authorization.entity.SysRole;
import lombok.Data;

import java.util.List;

/**
 * 用户DTO，包含角色信息
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UserDTO {
  /**
   * 用户基本信息（不包含密码）
   */
  private UserResponseDTO user;

  /**
   * 用户角色列表
   */
  private List<SysRole> roles;

  /**
   * 数据域解析结果（多角色并集），供业务侧行级过滤参考
   */
  private ResolvedDataScopeDTO dataScope;
}


package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageRequest extends PageRequest {
  /**
   * 角色名称（模糊搜索）
   */
  private String name;

  /**
   * 备注（模糊搜索）
   */
  private String remark;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}


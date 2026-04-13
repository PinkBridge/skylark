package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 组织分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationPageRequest extends PageRequest {
  /**
   * 组织名称（模糊搜索）
   */
  private String name;

  /**
   * 组织编码（模糊搜索）
   */
  private String code;

  /**
   * 联系电话（模糊搜索）
   */
  private String phone;

  /**
   * 邮箱（模糊搜索）
   */
  private String email;

  /**
   * 组织类型（精确搜索）
   */
  private String type;

  /**
   * 状态（精确搜索）
   */
  private String status;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}


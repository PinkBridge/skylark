package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户分页查询请求参数
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantPageRequest extends PageRequest {

  /**
   * 租户名称（模糊搜索）
   */
  private String name;

  /**
   * 租户编码（模糊搜索）
   */
  private String code;

  /**
   * 联系人电话（模糊搜索）
   */
  private String contactPhone;

  /**
   * 联系人邮箱（模糊搜索）
   */
  private String contactEmail;

  /**
   * 租户域名（模糊搜索）
   */
  private String domain;

  /**
   * 状态（精确搜索）
   */
  private String status;

  /**
   * 创建时间（查询此时间之前的数据）
   */
  private LocalDateTime createTime;
}



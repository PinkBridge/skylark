package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 更新组织信息DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UpdateOrganizationDTO {
  /**
   * 组织名称
   */
  private String name;

  /**
   * 组织编码
   */
  private String code;

  /**
   * 父组织ID
   */
  private Long parentId;

  /**
   * 组织类型：COMPANY-公司，DEPARTMENT-部门，TEAM-小组
   */
  private String type;

  /**
   * 层级
   */
  private Integer level;

  /**
   * 排序
   */
  private Integer sort;

  /**
   * 负责人
   */
  private String leader;

  /**
   * 联系电话
   */
  private String phone;

  /**
   * 邮箱
   */
  private String email;

  /**
   * 地址
   */
  private String address;

  /**
   * 描述
   */
  private String description;

  /**
   * 状态：ACTIVE-启用，INACTIVE-禁用
   */
  private String status;
}


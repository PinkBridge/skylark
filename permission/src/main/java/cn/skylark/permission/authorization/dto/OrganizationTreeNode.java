package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 组织树节点DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class OrganizationTreeNode {
  /**
   * 组织ID
   */
  private Long id;

  /**
   * 父组织ID
   */
  private Long parentId;

  /**
   * 父组织信息
   */
  private ParentOrganizationDTO parentOrganization;

  /**
   * 组织名称
   */
  private String name;

  /**
   * 组织编码
   */
  private String code;

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

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 租户名称（列表展示用）
   */
  private String tenantName;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;

  /**
   * 子组织列表
   */
  private List<OrganizationTreeNode> children = new ArrayList<>();
}


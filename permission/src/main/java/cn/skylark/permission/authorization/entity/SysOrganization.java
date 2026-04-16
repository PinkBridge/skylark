package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统组织实体
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class SysOrganization {
  /**
   * 组织ID
   */
  private Long id;

  /**
   * 组织名称
   */
  private String name;

  /**
   * 组织编码（唯一）
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

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 软删除标记：0-未删除，1-已删除
   */
  private Boolean isDelete;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}


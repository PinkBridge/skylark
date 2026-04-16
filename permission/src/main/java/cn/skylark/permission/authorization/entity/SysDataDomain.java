package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据域实体
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Data
public class SysDataDomain {
  /**
   * 主键ID
   */
  private Long id;

  /**
   * 数据域名称
   */
  private String name;

  /**
   * 数据域编码（唯一）
   */
  private String code;

  /**
   * 数据范围类型：ALL-全部数据，TENANT-本租户全部，ORG_ALL-本组织及所有下级，ORG_AND_CHILD-本组织及直接下级，ORG_ONLY-仅本组织，SELF-仅本人，CUSTOM-自定义
   */
  private String type;

  /**
   * 数据域范围值（JSON格式，存储组织ID列表等）
   */
  private String scopeValue;

  /**
   * 自定义SQL规则（当type为CUSTOM时使用）
   */
  private String customSql;

  /**
   * 描述
   */
  private String description;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 是否启用：1-启用，0-禁用
   */
  private Boolean enabled;

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


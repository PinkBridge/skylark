package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据域响应DTO
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Data
public class DataDomainResponseDTO {
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
   * 数据范围类型
   */
  private String type;

  /**
   * 数据域范围值（JSON格式）
   */
  private String scopeValue;

  /**
   * 自定义SQL规则
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
   * 是否启用
   */
  private Boolean enabled;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}


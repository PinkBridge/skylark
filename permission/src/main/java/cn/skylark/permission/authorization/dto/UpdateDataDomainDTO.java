package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 更新数据域信息DTO
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Data
public class UpdateDataDomainDTO {
  /**
   * 数据域名称
   */
  private String name;

  /**
   * 数据域编码
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
   * 是否启用
   */
  private Boolean enabled;
}


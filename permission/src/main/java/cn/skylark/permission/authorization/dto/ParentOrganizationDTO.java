package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 父组织信息DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class ParentOrganizationDTO {
  /**
   * 组织ID
   */
  private Long id;

  /**
   * 组织名称
   */
  private String name;

  /**
   * 组织编码
   */
  private String code;

  /**
   * 组织类型
   */
  private String type;
}


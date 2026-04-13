package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 更新资源信息DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UpdateResourceDTO {
  /**
   * 文件名
   */
  private String name;

  /**
   * 描述
   */
  private String description;
}


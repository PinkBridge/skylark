package cn.skylark.permission.authorization.dto;

import lombok.Data;

/**
 * 更新API信息DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class UpdateApiDTO {
  /**
   * HTTP方法
   */
  private String method;

  /**
   * API路径
   */
  private String path;

  /**
   * 权限标签
   */
  private String permlabel;

  /**
   * 模块键
   */
  private String moduleKey;
}


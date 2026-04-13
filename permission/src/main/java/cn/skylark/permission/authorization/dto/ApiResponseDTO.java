package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API响应DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class ApiResponseDTO {
  /**
   * API ID
   */
  private Long id;

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

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;
}


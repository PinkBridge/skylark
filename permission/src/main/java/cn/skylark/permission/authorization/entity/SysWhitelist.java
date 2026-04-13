package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统白名单实体
 * 用于放权不需要权限验证的API
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class SysWhitelist {
  /**
   * 白名单ID
   */
  private Long id;

  /**
   * HTTP方法：GET, POST, PUT, DELETE等，支持ALL表示所有方法
   */
  private String method;

  /**
   * API路径，支持Ant风格通配符（如：/api/public/**）
   */
  private String path;

  /**
   * 备注说明
   */
  private String remark;

  /**
   * 是否启用：1-启用，0-禁用
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


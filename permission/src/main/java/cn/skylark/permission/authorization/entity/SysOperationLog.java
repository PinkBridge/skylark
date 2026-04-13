package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * HTTP 接口操作记录（仅存技术字段，不含业务语义）
 */
@Data
public class SysOperationLog {
  private Long id;
  private Long tenantId;
  private Long userId;
  private String username;
  private String httpMethod;
  private String requestUri;
  private Integer httpStatus;
  private Long durationMs;
  private String clientIp;
  private LocalDateTime createdAt;
}

package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogResponseDTO {
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

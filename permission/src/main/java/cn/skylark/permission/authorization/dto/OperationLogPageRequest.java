package cn.skylark.permission.authorization.dto;

import cn.skylark.permission.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogPageRequest extends PageRequest {
  private String username;
  private String httpMethod;
  private Integer httpStatus;
  private String requestUri;
  private LocalDateTime createdTimeStart;
  private LocalDateTime createdTimeEnd;
  private Long tenantId;
}

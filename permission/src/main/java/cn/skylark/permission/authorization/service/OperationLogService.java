package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.OperationLogPageRequest;
import cn.skylark.permission.authorization.dto.OperationLogResponseDTO;
import cn.skylark.permission.authorization.entity.SysOperationLog;
import cn.skylark.permission.authorization.entity.SysUser;
import cn.skylark.permission.authorization.mapper.OperationLogMapper;
import cn.skylark.permission.utils.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OperationLogService {

  @Resource
  private OperationLogMapper operationLogMapper;

  @Resource
  private UserService userService;

  @Async("operationLogExecutor")
  public void recordAsync(Long tenantId, String username, String httpMethod, String requestUri,
                          int httpStatus, long durationMs, String clientIp) {
    try {
      Long userId = null;
      if (StringUtils.hasText(username)) {
        SysUser u = userService.findByUsername(username);
        if (u != null) {
          userId = u.getId();
        }
      }
      SysOperationLog row = new SysOperationLog();
      row.setTenantId(tenantId);
      row.setUserId(userId);
      row.setUsername(username);
      row.setHttpMethod(httpMethod);
      row.setRequestUri(requestUri);
      row.setHttpStatus(httpStatus);
      row.setDurationMs(durationMs);
      row.setClientIp(clientIp);
      operationLogMapper.insert(row);
    } catch (Exception e) {
      log.warn("operation log insert failed: {} {}", httpMethod, requestUri, e);
    }
  }

  public OperationLogResponseDTO getDTO(Long id) {
    SysOperationLog row = operationLogMapper.selectById(id);
    return toDto(row);
  }

  public PageResult<OperationLogResponseDTO> pageDTOWithCondition(OperationLogPageRequest pageRequest) {
    boolean hasCondition = StringUtils.hasText(pageRequest.getUsername())
        || StringUtils.hasText(pageRequest.getHttpMethod())
        || pageRequest.getHttpStatus() != null
        || StringUtils.hasText(pageRequest.getRequestUri())
        || pageRequest.getCreatedTimeStart() != null
        || pageRequest.getCreatedTimeEnd() != null
        || pageRequest.getTenantId() != null;

    List<SysOperationLog> records;
    Long total;
    if (hasCondition) {
      records = operationLogMapper.selectPageWithCondition(
          pageRequest.getUsername(),
          pageRequest.getHttpMethod(),
          pageRequest.getHttpStatus(),
          pageRequest.getRequestUri(),
          pageRequest.getCreatedTimeStart(),
          pageRequest.getCreatedTimeEnd(),
          pageRequest.getTenantId(),
          pageRequest.getOffset(),
          pageRequest.getLimit());
      total = operationLogMapper.countWithCondition(
          pageRequest.getUsername(),
          pageRequest.getHttpMethod(),
          pageRequest.getHttpStatus(),
          pageRequest.getRequestUri(),
          pageRequest.getCreatedTimeStart(),
          pageRequest.getCreatedTimeEnd(),
          pageRequest.getTenantId());
    } else {
      records = operationLogMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = operationLogMapper.countAll();
    }
    List<OperationLogResponseDTO> dtoList = records.stream().map(this::toDto).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  private OperationLogResponseDTO toDto(SysOperationLog row) {
    if (row == null) {
      return null;
    }
    OperationLogResponseDTO dto = new OperationLogResponseDTO();
    BeanUtils.copyProperties(row, dto);
    return dto;
  }
}

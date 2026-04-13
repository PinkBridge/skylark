package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.OperationLogPageRequest;
import cn.skylark.permission.authorization.dto.OperationLogResponseDTO;
import cn.skylark.permission.authorization.service.OperationLogService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/permission/operation-logs")
public class OperationLogController {

  @Resource
  private OperationLogService operationLogService;

  @GetMapping("/{id}")
  public Ret<OperationLogResponseDTO> get(@PathVariable Long id) {
    OperationLogResponseDTO dto = operationLogService.getDTO(id);
    if (dto == null) {
      return Ret.fail(404, "operation.log.not.found");
    }
    return Ret.data(dto);
  }

  @GetMapping("/page")
  public Ret<PageResult<OperationLogResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String httpMethod,
      @RequestParam(required = false) Integer httpStatus,
      @RequestParam(required = false) String requestUri,
      @RequestParam(required = false) LocalDateTime createdTimeStart,
      @RequestParam(required = false) LocalDateTime createdTimeEnd,
      @RequestParam(required = false) Long tenantId) {
    OperationLogPageRequest req = new OperationLogPageRequest();
    req.setPage(page);
    req.setSize(size);
    req.setUsername(username);
    req.setHttpMethod(httpMethod);
    req.setHttpStatus(httpStatus);
    req.setRequestUri(requestUri);
    req.setCreatedTimeStart(createdTimeStart);
    req.setCreatedTimeEnd(createdTimeEnd);
    req.setTenantId(tenantId);
    return Ret.data(operationLogService.pageDTOWithCondition(req));
  }
}

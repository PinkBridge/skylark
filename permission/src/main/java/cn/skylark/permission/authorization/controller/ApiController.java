package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.ApiPageRequest;
import cn.skylark.permission.authorization.dto.ApiResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateApiDTO;
import cn.skylark.permission.authorization.entity.SysApi;
import cn.skylark.permission.authorization.service.ApiService;
import cn.skylark.permission.authorization.service.TenantPermissionCeilingService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yaomianwei
 */
@RestController
@RequestMapping("/api/permission/apis")
public class ApiController {

  @Resource
  private ApiService apiService;

  @Resource
  private TenantPermissionCeilingService ceilingService;

  @GetMapping("/{id}")
  public Ret<ApiResponseDTO> get(@PathVariable Long id) {
    ApiResponseDTO apiDTO = apiService.getDTO(id);
    if (apiDTO == null) {
      return Ret.fail(404, "api.not.found");
    }
    return Ret.data(apiDTO);
  }

  @GetMapping
  public Ret<List<ApiResponseDTO>> list(@RequestParam(required = false) String app) {
    return Ret.data(apiService.listDTO(app));
  }

  /**
   * 租户侧“可授权范围”API 列表：返回“租户管理员上限”内的 API。
   * 平台/超管上下文下返回全量列表。
   */
  @GetMapping("/grantable")
  public Ret<List<SysApi>> grantableList(@RequestParam(required = false) String app) {
    Long tenantId = ceilingService.resolveTenantIdOrNull();
    if (tenantId == null) {
      return Ret.data(apiService.list(app));
    }
    Long adminRoleId = ceilingService.tenantAdminRoleId(tenantId);
    if (adminRoleId == null) {
      return Ret.data(java.util.Collections.emptyList());
    }
    return Ret.data(apiService.listByRole(adminRoleId, app));
  }

  /**
   * 分页查询API列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param method     HTTP方法（模糊搜索）
   * @param path       API路径（模糊搜索）
   * @param permlabel  权限标签（模糊搜索）
   * @param moduleKey  模块键（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<ApiResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String path,
      @RequestParam(required = false) String permlabel,
      @RequestParam(required = false) String moduleKey,
      @RequestParam(required = false) String app,
      @RequestParam(required = false) LocalDateTime createTime) {
    ApiPageRequest pageRequest = new ApiPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setMethod(method);
    pageRequest.setPath(path);
    pageRequest.setPermlabel(permlabel);
    pageRequest.setModuleKey(moduleKey);
    pageRequest.setApp(app);
    pageRequest.setCreateTime(createTime);
    return Ret.data(apiService.pageDTOWithCondition(pageRequest));
  }

  @PostMapping
  public Ret<Integer> create(@RequestBody SysApi api) {
    return Ret.data(apiService.create(api));
  }

  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateApiDTO updateApiDTO) {
    ApiResponseDTO apiDTO = apiService.getDTO(id);
    if (apiDTO == null) {
      return Ret.fail(404, "api.not.found");
    }
    return Ret.data(apiService.updateApiInfo(id, updateApiDTO));
  }

  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    return Ret.data(apiService.delete(id));
  }
}

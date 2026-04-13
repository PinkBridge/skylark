package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.MenuIdsDTO;
import cn.skylark.permission.authorization.dto.RolePageRequest;
import cn.skylark.permission.authorization.dto.RoleResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateRoleDTO;
import cn.skylark.permission.authorization.entity.SysApi;
import cn.skylark.permission.authorization.entity.SysMenu;
import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.service.ApiService;
import cn.skylark.permission.authorization.service.MenuService;
import cn.skylark.permission.authorization.service.DataDomainService;
import cn.skylark.permission.authorization.service.RoleService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author yaomianwei
 */
@RestController
@RequestMapping("/api/permission/roles")
public class RoleController {

  @Resource
  private RoleService roleService;
  @Resource
  private MenuService menuService;
  @Resource
  private ApiService apiService;
  @Resource
  private DataDomainService dataDomainService;

  @GetMapping
  public Ret<List<RoleResponseDTO>> list() {
    return Ret.data(roleService.listDTO());
  }

  /**
   * 分页查询角色列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param name       角色名称（模糊搜索）
   * @param remark     备注（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<RoleResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String remark,
      @RequestParam(required = false) LocalDateTime createTime) {
    RolePageRequest pageRequest = new RolePageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setName(name);
    pageRequest.setRemark(remark);
    pageRequest.setCreateTime(createTime);
    return Ret.data(roleService.pageDTOWithCondition(pageRequest));
  }

  @GetMapping("/{id}")
  public Ret<RoleResponseDTO> get(@PathVariable Long id) {
    RoleResponseDTO roleDTO = roleService.getDTO(id);
    if (roleDTO == null) {
      return Ret.fail(404, "role.not.found");
    }
    return Ret.data(roleDTO);
  }

  @PostMapping
  public Ret<Integer> create(@RequestBody SysRole role) {
    return Ret.data(roleService.create(role));
  }

  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateRoleDTO updateRoleDTO) {
    RoleResponseDTO roleDTO = roleService.getDTO(id);
    if (roleDTO == null) {
      return Ret.fail(404, "role.not.found");
    }
    return Ret.data(roleService.updateRoleInfo(id, updateRoleDTO));
  }

  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    return Ret.data(roleService.delete(id));
  }

  @GetMapping("/{id}/menus")
  public Ret<List<SysMenu>> roleMenus(@PathVariable Long id) {
    return Ret.data(menuService.listByRole(id));
  }

  @PostMapping("/{id}/menus:bind")
  public Ret<Void> bindMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
    roleService.bindMenus(id, menuIds);
    return Ret.ok();
  }

  @GetMapping("/{id}/apis")
  public Ret<List<SysApi>> roleApis(@PathVariable Long id) {
    return Ret.data(apiService.listByRole(id));
  }

  @PostMapping("/{id}/apis:bind")
  public Ret<Void> bindApis(@PathVariable Long id, @RequestBody List<Long> apiIds) {
    roleService.bindApis(id, apiIds);
    return Ret.ok();
  }

  @GetMapping("/{id}/data-domains")
  public Ret<List<SysDataDomain>> roleDataDomains(@PathVariable Long id) {
    return Ret.data(dataDomainService.listByRole(id));
  }

  @PostMapping("/{id}/data-domains:bind")
  public Ret<Void> bindDataDomains(@PathVariable Long id, @RequestBody List<Long> dataDomainIds) {
    try {
      roleService.bindDataDomains(id, dataDomainIds);
      return Ret.ok();
    } catch (IllegalArgumentException e) {
      return Ret.fail(400, e.getMessage());
    }
  }

  /**
   * 切换角色和API的关联状态
   * 如果关联不存在则添加，如果存在则删除
   *
   * @param roleId 角色ID
   * @param apiId  API ID
   * @return 操作结果，true-添加了关联，false-删除了关联
   */
  @PostMapping("/{roleId}/apis/{apiId}:toggle")
  public Ret<Boolean> toggleRoleApi(@PathVariable Long roleId, @PathVariable Long apiId) {
    boolean added = apiService.toggleRoleApiBinding(roleId, apiId);
    return Ret.data(added);
  }

  /**
   * 批量切换角色和菜单的关联状态
   * 对于每个菜单ID，如果关联不存在则添加，如果存在则删除
   *
   * @param roleId  角色ID
   * @param request 请求体，包含菜单ID数组，格式：{"menuIds": [11, 12, 13]}
   * @return 操作结果，key为菜单ID，value为true表示添加了关联，false表示删除了关联
   */
  @PostMapping("/{roleId}/menus:toggle")
  public Ret<Map<Long, Boolean>> toggleRoleMenus(@PathVariable Long roleId,
                                                 @RequestBody MenuIdsDTO request) {
    if (request == null || request.getMenuIds() == null || request.getMenuIds().isEmpty()) {
      return Ret.fail(400, "menuIds.required");
    }
    Map<Long, Boolean> results = menuService.toggleRoleMenuBindings(roleId, request.getMenuIds());
    return Ret.data(results);
  }
}

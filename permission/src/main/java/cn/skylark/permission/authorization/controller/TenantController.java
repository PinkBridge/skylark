package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.dto.TenantPageRequest;
import cn.skylark.permission.authorization.dto.TenantResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateTenantDTO;
import cn.skylark.permission.authorization.dto.CreateTenantAdminDTO;
import cn.skylark.permission.authorization.dto.TenantInitializeRequestDTO;
import cn.skylark.permission.authorization.dto.TenantInitInfoDTO;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.service.TenantService;
import cn.skylark.permission.utils.PageResult;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户控制器
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@RestController
@RequestMapping("/api/permission/tenants")
public class TenantController {

  @Resource
  private TenantService tenantService;

  @GetMapping
  public Ret<List<TenantResponseDTO>> list() {
    return Ret.data(tenantService.listDTO());
  }

  /**
   * 分页查询租户列表（支持搜索）
   *
   * @param page       页码，从1开始，默认1
   * @param size       每页大小，默认10
   * @param name       租户名称（模糊搜索）
   * @param code       租户编码（模糊搜索）
   * @param contactPhone 联系人电话（模糊搜索）
   * @param contactEmail 联系人邮箱（模糊搜索）
   * @param domain     租户域名（模糊搜索）
   * @param status     状态（精确搜索）
   * @param createTime 创建时间（查询此时间之前的数据，支持格式：ISO 8601如2025-11-22T16:00:00.000Z，或yyyy-MM-dd HH:mm:ss）
   * @return 分页结果
   */
  @GetMapping("/page")
  public Ret<PageResult<TenantResponseDTO>> page(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String contactPhone,
      @RequestParam(required = false) String contactEmail,
      @RequestParam(required = false) String domain,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDateTime createTime) {
    TenantPageRequest pageRequest = new TenantPageRequest();
    pageRequest.setPage(page);
    pageRequest.setSize(size);
    pageRequest.setName(name);
    pageRequest.setCode(code);
    pageRequest.setContactPhone(contactPhone);
    pageRequest.setContactEmail(contactEmail);
    pageRequest.setDomain(domain);
    pageRequest.setStatus(status);
    pageRequest.setCreateTime(createTime);
    return Ret.data(tenantService.pageDTOWithCondition(pageRequest));
  }

  @GetMapping("/{id}")
  public Ret<TenantResponseDTO> get(@PathVariable Long id) {
    TenantResponseDTO tenantDTO = tenantService.getDTO(id);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    return Ret.data(tenantDTO);
  }

  @GetMapping("/me")
  public Ret<TenantResponseDTO> getMyTenant() {
    TenantResponseDTO tenantDTO = tenantService.getCurrentTenantDTO();
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    return Ret.data(tenantDTO);
  }

  /**
   * 根据域名查询租户信息
   *
   * @param domain 租户访问域名
   * @return 租户信息
   */
  @GetMapping("/domain/{domain}")
  public Ret<TenantResponseDTO> getByDomain(@PathVariable String domain) {
    TenantResponseDTO tenantDTO = tenantService.getDTOByDomain(domain);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    return Ret.data(tenantDTO);
  }

  @PostMapping
  public Ret<Integer> create(@RequestBody SysTenant tenant) {
    if (tenant != null) {
      tenant.setDomain(tenantService.normalizeDomainValue(tenant.getDomain()));
    }
    if (tenantService.isDomainDuplicated(tenant == null ? null : tenant.getDomain(), null)) {
      return Ret.fail(400, "tenant.domain.duplicate");
    }
    return Ret.data(tenantService.create(tenant));
  }

  @PutMapping("/{id}")
  public Ret<Integer> update(@PathVariable Long id, @RequestBody UpdateTenantDTO updateTenantDTO) {
    TenantResponseDTO tenantDTO = tenantService.getDTO(id);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    if (updateTenantDTO != null) {
      updateTenantDTO.setDomain(tenantService.normalizeDomainValue(updateTenantDTO.getDomain()));
    }
    if (tenantService.isDomainDuplicated(updateTenantDTO == null ? null : updateTenantDTO.getDomain(), id)) {
      return Ret.fail(400, "tenant.domain.duplicate");
    }
    return Ret.data(tenantService.updateTenantInfo(id, updateTenantDTO));
  }

  @PutMapping("/me")
  public Ret<Integer> updateMyTenant(@RequestBody UpdateTenantDTO updateTenantDTO) {
    if (updateTenantDTO != null) {
      updateTenantDTO.setDomain(tenantService.normalizeDomainValue(updateTenantDTO.getDomain()));
    }
    try {
      return Ret.data(tenantService.updateCurrentTenantInfo(updateTenantDTO));
    } catch (IllegalArgumentException e) {
      String message = e.getMessage();
      if ("tenant.not.found".equals(message)) {
        return Ret.fail(404, message);
      }
      return Ret.fail(400, message);
    }
  }

  @DeleteMapping("/{id}")
  public Ret<Integer> delete(@PathVariable Long id) {
    return Ret.data(tenantService.delete(id));
  }

  @PostMapping("/{id}/admin")
  public Ret<Long> createTenantAdmin(@PathVariable Long id, @RequestBody CreateTenantAdminDTO dto) {
    TenantResponseDTO tenantDTO = tenantService.getDTO(id);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    try {
      Long userId = tenantService.createTenantAdminUser(id,
          dto == null ? null : dto.getUsername(),
          dto == null ? null : dto.getPassword(),
          dto == null ? null : dto.getRoleId());
      if (userId == null) {
        return Ret.fail(500, "tenant.admin.create.failed");
      }
      return Ret.data(userId);
    } catch (IllegalArgumentException e) {
      return Ret.fail(400, e.getMessage());
    }
  }

  /**
   * 平台侧：初始化租户默认组织与默认登录用户（已初始化则拒绝）。
   */
  @PostMapping("/{id}/initialize")
  public Ret<Long> initializeTenant(@PathVariable Long id, @RequestBody TenantInitializeRequestDTO body) {
    TenantResponseDTO tenantDTO = tenantService.getDTO(id);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    try {
      Long userId = tenantService.initializeTenant(id, body);
      return Ret.data(userId);
    } catch (IllegalArgumentException e) {
      return Ret.fail(400, e.getMessage());
    } catch (IllegalStateException e) {
      return Ret.fail(500, e.getMessage());
    }
  }

  /**
   * 平台侧：查询租户初始化信息（默认组织 + 默认用户 + 角色）。
   */
  @GetMapping("/{id}/init-info")
  public Ret<TenantInitInfoDTO> initInfo(@PathVariable Long id) {
    TenantResponseDTO tenantDTO = tenantService.getDTO(id);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    return Ret.data(tenantService.initInfo(id));
  }

  /**
   * 平台侧：查询某租户下的角色列表（用于创建租户管理员时选择“上限来源角色”）。
   */
  @GetMapping("/{id}/roles")
  public Ret<List<cn.skylark.permission.authorization.entity.SysRole>> rolesByTenant(@PathVariable Long id) {
    TenantResponseDTO tenantDTO = tenantService.getDTO(id);
    if (tenantDTO == null) {
      return Ret.fail(404, "tenant.not.found");
    }
    return Ret.data(tenantService.listRolesByTenantId(id));
  }
}



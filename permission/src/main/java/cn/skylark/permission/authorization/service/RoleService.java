package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.dto.RolePageRequest;
import cn.skylark.permission.authorization.dto.RoleResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateRoleDTO;
import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysTenant;
import cn.skylark.permission.authorization.mapper.RoleMapper;
import cn.skylark.permission.authorization.mapper.TenantMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleService {

  @Resource
  private RoleMapper roleMapper;
  @Resource
  private MenuService menuService;
  @Resource
  private ApiService apiService;
  @Resource
  private DataDomainService dataDomainService;

  @Resource
  private TenantRolePermissionGuard tenantRolePermissionGuard;

  @Resource
  private TenantMapper tenantMapper;

  public SysRole get(Long id) {
    return roleMapper.selectById(id);
  }

  public List<SysRole> list() {
    return roleMapper.selectAll();
  }

  public int create(SysRole role) {
    if (role.getTenantId() == null) {
      role.setTenantId(TenantContext.getTenantId());
    }
    return roleMapper.insert(role);
  }

  public int update(SysRole role) {
    return roleMapper.update(role);
  }

  public int delete(Long id) {
    return roleMapper.deleteById(id);
  }

  public void bindMenus(Long roleId, List<Long> menuIds) {
    tenantRolePermissionGuard.validateRoleMenusWithinCeiling(roleId, menuIds);
    menuService.bindRoleMenus(roleId, menuIds);
  }

  public void bindApis(Long roleId, List<Long> apiIds) {
    tenantRolePermissionGuard.validateRoleApisWithinCeiling(roleId, apiIds);
    apiService.bindRoleApis(roleId, apiIds);
  }

  public void bindDataDomains(Long roleId, List<Long> dataDomainIds) {
    tenantRolePermissionGuard.validateRoleDataDomainsWithinCeiling(roleId, dataDomainIds);
    dataDomainService.bindRoleDataDomains(roleId, dataDomainIds);
  }

  /**
   * 获取角色列表（DTO）
   *
   * @return 角色列表
   */
  public List<RoleResponseDTO> listDTO() {
    List<SysRole> roles = roleMapper.selectAll();
    Map<Long, String> tenantNames = buildTenantNameMap();
    return roles.stream().map(r -> convertToDTO(r, tenantNames)).collect(Collectors.toList());
  }

  /**
   * 获取角色信息（DTO）
   *
   * @param id 角色ID
   * @return 角色信息
   */
  public RoleResponseDTO getDTO(Long id) {
    SysRole role = roleMapper.selectById(id);
    if (role == null) {
      return null;
    }
    RoleResponseDTO dto = convertToDTO(role, buildTenantNameMap());
    // 获取关联的菜单ID数组
    List<Long> menuIds = menuService.listByRole(id).stream()
        .map(menu -> menu.getId())
        .collect(Collectors.toList());
    dto.setMenuIds(menuIds);
    // 获取关联的API ID数组
    List<Long> apiIds = apiService.listByRole(id).stream()
        .map(api -> api.getId())
        .collect(Collectors.toList());
    dto.setApiIds(apiIds);
    List<Long> dataDomainIds = dataDomainService.listByRole(id).stream()
        .map(SysDataDomain::getId)
        .collect(Collectors.toList());
    dto.setDataDomainIds(dataDomainIds);
    return dto;
  }

  /**
   * 分页查询角色列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<RoleResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysRole> records = roleMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = roleMapper.countAll();
    Map<Long, String> tenantNames = buildTenantNameMap();
    List<RoleResponseDTO> dtoList = records.stream().map(r -> convertToDTO(r, tenantNames)).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询角色列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<RoleResponseDTO> pageDTOWithCondition(RolePageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getName()) ||
                           StringUtils.hasText(pageRequest.getRemark()) ||
                           pageRequest.getCreateTime() != null;

    List<SysRole> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = roleMapper.selectPageWithCondition(
          pageRequest.getName(),
          pageRequest.getRemark(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = roleMapper.countWithCondition(
          pageRequest.getName(),
          pageRequest.getRemark(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = roleMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = roleMapper.countAll();
    }

    Map<Long, String> tenantNames = buildTenantNameMap();
    List<RoleResponseDTO> dtoList = records.stream().map(r -> convertToDTO(r, tenantNames)).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新角色信息
   *
   * @param roleId 角色ID
   * @param dto    更新角色信息DTO
   * @return 更新行数
   */
  public int updateRoleInfo(Long roleId, UpdateRoleDTO dto) {
    return roleMapper.updateRoleInfo(roleId, dto);
  }

  private Map<Long, String> buildTenantNameMap() {
    Map<Long, String> map = new HashMap<>();
    List<SysTenant> tenants = tenantMapper.selectAll();
    if (tenants == null) {
      return map;
    }
    for (SysTenant t : tenants) {
      if (t != null && t.getId() != null) {
        map.put(t.getId(), t.getName());
      }
    }
    return map;
  }

  private RoleResponseDTO convertToDTO(SysRole role, Map<Long, String> tenantNames) {
    if (role == null) {
      return null;
    }
    RoleResponseDTO dto = new RoleResponseDTO();
    BeanUtils.copyProperties(role, dto);
    if (role.getTenantId() != null && tenantNames != null) {
      dto.setTenantName(tenantNames.get(role.getTenantId()));
    }
    return dto;
  }
}

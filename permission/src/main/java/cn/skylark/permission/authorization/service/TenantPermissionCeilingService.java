package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.context.TenantContext;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysTenantAdminBinding;
import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.mapper.ApiMapper;
import cn.skylark.permission.authorization.mapper.DataDomainMapper;
import cn.skylark.permission.authorization.mapper.MenuMapper;
import cn.skylark.permission.authorization.mapper.RoleMapper;
import cn.skylark.permission.authorization.mapper.TenantAdminBindingMapper;
import cn.skylark.permission.authorization.support.TenantRoleConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * “租户权限上限”解析：以该租户的“租户管理员”角色拥有的菜单/API 作为上限集合。
 */
@Service
public class TenantPermissionCeilingService {

  @Resource
  private RoleMapper roleMapper;

  @Resource
  private TenantAdminBindingMapper tenantAdminBindingMapper;

  @Resource
  private MenuMapper menuMapper;

  @Resource
  private ApiMapper apiMapper;

  @Resource
  private DataDomainMapper dataDomainMapper;

  public Long resolveTenantIdOrNull() {
    return TenantContext.getTenantId();
  }

  public SysTenantAdminBinding tenantAdminBinding(Long tenantId) {
    if (tenantId == null) {
      return null;
    }
    return tenantAdminBindingMapper.selectByTenantId(tenantId);
  }

  public Long tenantAdminRoleId(Long tenantId) {
    if (tenantId == null) {
      return null;
    }
    SysTenantAdminBinding binding = tenantAdminBinding(tenantId);
    if (binding != null && binding.getRoleId() != null) {
      return binding.getRoleId();
    }
    // Fallback for legacy data (should be eliminated by V25 backfill).
    SysRole role = roleMapper.selectByNameAndTenantId(TenantRoleConstants.TENANT_ADMIN_ROLE_NAME, tenantId);
    return role == null ? null : role.getId();
  }

  public Set<Long> allowedMenuIds(Long tenantId) {
    Long adminRoleId = tenantAdminRoleId(tenantId);
    if (adminRoleId == null) {
      return Collections.emptySet();
    }
    List<Long> ids = menuMapper.selectByRoleId(adminRoleId).stream()
        .map(m -> m == null ? null : m.getId())
        .collect(Collectors.toList());
    if (ids == null || ids.isEmpty()) {
      return Collections.emptySet();
    }
    return ids.stream().filter(id -> id != null).collect(Collectors.toCollection(HashSet::new));
  }

  public Set<Long> allowedApiIds(Long tenantId) {
    Long adminRoleId = tenantAdminRoleId(tenantId);
    if (adminRoleId == null) {
      return Collections.emptySet();
    }
    List<Long> ids = apiMapper.selectByRoleId(adminRoleId).stream()
        .map(a -> a == null ? null : a.getId())
        .collect(Collectors.toList());
    if (ids == null || ids.isEmpty()) {
      return Collections.emptySet();
    }
    return ids.stream().filter(id -> id != null).collect(Collectors.toCollection(HashSet::new));
  }

  /**
   * 租户管理员角色已绑定的数据域 id 集合，作为子角色可绑定的上限。
   */
  public Set<Long> allowedDataDomainIds(Long tenantId) {
    Long adminRoleId = tenantAdminRoleId(tenantId);
    if (adminRoleId == null) {
      return Collections.emptySet();
    }
    List<SysDataDomain> list = dataDomainMapper.selectByRoleId(adminRoleId);
    if (list == null || list.isEmpty()) {
      return Collections.emptySet();
    }
    return list.stream()
        .map(SysDataDomain::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(HashSet::new));
  }
}


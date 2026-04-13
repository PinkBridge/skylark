package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.mapper.RoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 租户侧角色授权守卫：子角色只能从“租户管理员”上限内选择菜单/API。
 */
@Service
public class TenantRolePermissionGuard {

  @Resource
  private RoleMapper roleMapper;

  @Resource
  private TenantPermissionCeilingService ceilingService;

  @Resource
  private DataDomainService dataDomainService;

  public void validateRoleMenusWithinCeiling(Long roleId, List<Long> menuIds) {
    Long tenantId = ceilingService.resolveTenantIdOrNull();
    if (tenantId == null) {
      return; // 平台/超管上下文不限制
    }
    if (roleId == null) {
      throw new IllegalArgumentException("role.id.required");
    }
    SysRole role = roleMapper.selectById(roleId);
    if (role == null) {
      throw new IllegalArgumentException("role.not.found");
    }
    // 仅限制本租户的“非租户管理员”角色
    if (role.getTenantId() == null || !tenantId.equals(role.getTenantId())) {
      throw new IllegalArgumentException("role.tenant.mismatch");
    }
    Long adminRoleId = ceilingService.tenantAdminRoleId(tenantId);
    if (adminRoleId != null && adminRoleId.equals(role.getId())) {
      return;
    }
    if (CollectionUtils.isEmpty(menuIds)) {
      return;
    }
    Set<Long> allowed = ceilingService.allowedMenuIds(tenantId);
    for (Long id : menuIds) {
      if (id != null && !allowed.contains(id)) {
        throw new IllegalArgumentException("role.perms.exceed.tenant.admin");
      }
    }
  }

  public void validateRoleApisWithinCeiling(Long roleId, List<Long> apiIds) {
    Long tenantId = ceilingService.resolveTenantIdOrNull();
    if (tenantId == null) {
      return;
    }
    if (roleId == null) {
      throw new IllegalArgumentException("role.id.required");
    }
    SysRole role = roleMapper.selectById(roleId);
    if (role == null) {
      throw new IllegalArgumentException("role.not.found");
    }
    if (role.getTenantId() == null || !tenantId.equals(role.getTenantId())) {
      throw new IllegalArgumentException("role.tenant.mismatch");
    }
    Long adminRoleId = ceilingService.tenantAdminRoleId(tenantId);
    if (adminRoleId != null && adminRoleId.equals(role.getId())) {
      return;
    }
    if (CollectionUtils.isEmpty(apiIds)) {
      return;
    }
    Set<Long> allowed = ceilingService.allowedApiIds(tenantId);
    for (Long id : apiIds) {
      if (id != null && !allowed.contains(id)) {
        throw new IllegalArgumentException("role.perms.exceed.tenant.admin");
      }
    }
  }

  public void validateRoleDataDomainsWithinCeiling(Long roleId, List<Long> dataDomainIds) {
    Long tenantId = ceilingService.resolveTenantIdOrNull();
    if (tenantId == null) {
      return;
    }
    if (roleId == null) {
      throw new IllegalArgumentException("role.id.required");
    }
    SysRole role = roleMapper.selectById(roleId);
    if (role == null) {
      throw new IllegalArgumentException("role.not.found");
    }
    if (role.getTenantId() == null || !tenantId.equals(role.getTenantId())) {
      throw new IllegalArgumentException("role.tenant.mismatch");
    }
    Long adminRoleId = ceilingService.tenantAdminRoleId(tenantId);
    if (adminRoleId != null && adminRoleId.equals(role.getId())) {
      return;
    }
    if (CollectionUtils.isEmpty(dataDomainIds)) {
      return;
    }
    Set<Long> allowed = ceilingService.allowedDataDomainIds(tenantId);
    for (Long id : dataDomainIds) {
      if (id == null) {
        continue;
      }
      SysDataDomain d = dataDomainService.get(id);
      if (d == null || d.getTenantId() == null || !tenantId.equals(d.getTenantId())) {
        throw new IllegalArgumentException("data.domain.tenant.mismatch");
      }
      if (!allowed.contains(id)) {
        throw new IllegalArgumentException("role.perms.exceed.tenant.admin");
      }
    }
  }
}


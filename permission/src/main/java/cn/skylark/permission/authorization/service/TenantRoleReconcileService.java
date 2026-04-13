package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.entity.SysPermissionAudit;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.mapper.ApiMapper;
import cn.skylark.permission.authorization.mapper.MenuMapper;
import cn.skylark.permission.authorization.mapper.PermissionAuditMapper;
import cn.skylark.permission.authorization.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 当“租户管理员上限”缩小时，自动回收租户下其他角色中越界的菜单/API 绑定。
 */
@Slf4j
@Service
public class TenantRoleReconcileService {

  @Resource
  private RoleMapper roleMapper;

  @Resource
  private MenuMapper menuMapper;

  @Resource
  private ApiMapper apiMapper;

  @Resource
  private TenantPermissionCeilingService ceilingService;

  @Resource
  private PermissionAuditMapper permissionAuditMapper;

  @Transactional(rollbackFor = Exception.class)
  public ReconcileResult reconcileTenant(Long tenantId) {
    Long adminRoleId = ceilingService.tenantAdminRoleId(tenantId);
    if (tenantId == null || adminRoleId == null) {
      return new ReconcileResult(tenantId, 0, 0, 0);
    }
    Set<Long> allowedMenus = ceilingService.allowedMenuIds(tenantId);
    Set<Long> allowedApis = ceilingService.allowedApiIds(tenantId);

    List<SysRole> roles = roleMapper.selectByTenantId(tenantId);
    int affectedRoles = 0;
    int removedMenus = 0;
    int removedApis = 0;
    for (SysRole role : roles) {
      if (role == null || role.getId() == null) {
        continue;
      }
      if (role.getId().equals(adminRoleId)) {
        continue;
      }
      int rm = menuMapper.deleteRoleMenusNotInAllowed(role.getId(), new ArrayList<>(allowedMenus));
      int ra = apiMapper.deleteRoleApisNotInAllowed(role.getId(), new ArrayList<>(allowedApis));
      if (rm > 0 || ra > 0) {
        affectedRoles += 1;
        removedMenus += rm;
        removedApis += ra;
      }
    }

    writeAudit(tenantId, affectedRoles, removedMenus, removedApis);
    log.info("Tenant reconcile done: tenantId={}, affectedRoles={}, removedMenus={}, removedApis={}",
        tenantId, affectedRoles, removedMenus, removedApis);
    return new ReconcileResult(tenantId, affectedRoles, removedMenus, removedApis);
  }

  private void writeAudit(Long tenantId, int affectedRoles, int removedMenus, int removedApis) {
    try {
      SysPermissionAudit row = new SysPermissionAudit();
      row.setTenantId(tenantId);
      row.setOperator(resolveUsername());
      row.setAction("RECONCILE_TENANT");
      row.setDetail(String.format("{\"affectedRoles\":%d,\"removedMenus\":%d,\"removedApis\":%d}",
          affectedRoles, removedMenus, removedApis));
      permissionAuditMapper.insert(row);
    } catch (Exception e) {
      log.warn("permission audit insert failed", e);
    }
  }

  private static String resolveUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return auth.getName();
  }

  public static class ReconcileResult {
    public Long tenantId;
    public int affectedRoles;
    public int removedMenus;
    public int removedApis;

    public ReconcileResult(Long tenantId, int affectedRoles, int removedMenus, int removedApis) {
      this.tenantId = tenantId;
      this.affectedRoles = affectedRoles;
      this.removedMenus = removedMenus;
      this.removedApis = removedApis;
    }
  }
}


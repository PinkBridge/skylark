package cn.skylark.permission.authorization.controller;

import cn.skylark.permission.authorization.service.TenantRoleReconcileService;
import cn.skylark.permission.utils.Ret;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 一次性收敛：当平台调整租户管理员上限后，用于手工触发回收。
 * 建议仅超级管理员可用（通过 sys_api/permlabel 绑定限制）。
 */
@RestController
@RequestMapping("/api/permission/tenants/permissions")
public class TenantPermissionReconcileController {

  @Resource
  private TenantRoleReconcileService tenantRoleReconcileService;

  @PostMapping(":reconcile")
  public Ret<TenantRoleReconcileService.ReconcileResult> reconcile(@RequestParam Long tenantId) {
    return Ret.data(tenantRoleReconcileService.reconcileTenant(tenantId));
  }
}


package cn.skylark.permission.authorization.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TenantRoleConstants {

  private TenantRoleConstants() {
  }

  public static final String TENANT_ADMIN_ROLE_NAME = "租户管理员";
  public static final String TENANT_ADMIN_ROLE_REMARK = "租户级别管理员";

  /**
   * Defaults for tenant-admin role. These are used to backfill permissions when a tenant-admin role exists
   * but has not been bound to menus/APIs yet (e.g. old data or manual role creation).
   */
  public static final List<String> TENANT_ADMIN_MENU_PERMS = Collections.unmodifiableList(Arrays.asList(
      "perm.group",
      "perm.users.view", "perm.users.new", "perm.users.detail", "perm.users.edit", "perm.users.delete",
      "perm.orgs.view", "perm.orgs.new", "perm.orgs.detail", "perm.orgs.edit", "perm.orgs.delete",
      "perm.roles.view", "perm.roles.new", "perm.roles.detail", "perm.roles.edit", "perm.roles.delete", "perm.roles.api",
      "system.group",
      "system.tenant.profile.view", "system.tenant.profile.edit",
      "perm.resources.view", "perm.resources.detail", "perm.resources.delete",
      "logger.group",
      "logger.login.view", "logger.login.detail",
      "logger.operation.view", "logger.operation.detail"
  ));

  public static final List<String> TENANT_ADMIN_API_PERMS = Collections.unmodifiableList(Arrays.asList(
      "perm.users.detail", "perm.users.new", "perm.users.edit", "perm.users.delete",
      "perm.orgs.detail", "perm.orgs.new", "perm.orgs.edit", "perm.orgs.delete",
      "perm.roles.detail", "perm.roles.new", "perm.roles.edit", "perm.roles.delete", "perm.roles.api",
      "system.tenant.profile.detail", "system.tenant.profile.edit",
      "system.menus.detail", "system.apis.detail",
      "perm.resources.detail", "perm.resources.new", "perm.resources.delete",
      "logger.login.detail",
      "logger.operation.detail"
  ));
}


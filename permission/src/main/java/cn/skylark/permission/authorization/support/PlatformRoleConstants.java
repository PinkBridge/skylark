package cn.skylark.permission.authorization.support;

/**
 * Platform-level role names aligned with {@code sys_role.name} and Spring Security authorities.
 */
public final class PlatformRoleConstants {

  private PlatformRoleConstants() {
  }

  /** Seed in V5; used as authority via {@link cn.skylark.permission.authentication.service.CustomUserDetailsServiceImpl}. */
  public static final String SUPER_ADMIN_ROLE_NAME = "ROLE_SUPER_ADMIN";
}

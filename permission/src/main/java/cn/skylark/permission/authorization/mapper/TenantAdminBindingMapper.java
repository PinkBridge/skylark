package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysTenantAdminBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantAdminBindingMapper {
  SysTenantAdminBinding selectByTenantId(@Param("tenantId") Long tenantId);

  /**
   * 所有将 {@code roleId} 作为「权限上限角色」的租户（sys_tenant_admin_binding.role_id）。
   */
  List<Long> selectTenantIdsByCeilingRoleId(@Param("roleId") Long roleId);

  int upsert(@Param("tenantId") Long tenantId,
             @Param("userId") Long userId,
             @Param("roleId") Long roleId);
}


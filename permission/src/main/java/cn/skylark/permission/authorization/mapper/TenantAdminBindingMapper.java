package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysTenantAdminBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TenantAdminBindingMapper {
  SysTenantAdminBinding selectByTenantId(@Param("tenantId") Long tenantId);

  int upsert(@Param("tenantId") Long tenantId,
             @Param("userId") Long userId,
             @Param("roleId") Long roleId);
}


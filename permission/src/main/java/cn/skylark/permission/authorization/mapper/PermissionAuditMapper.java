package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysPermissionAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PermissionAuditMapper {
  int insert(@Param("row") SysPermissionAudit row);
}


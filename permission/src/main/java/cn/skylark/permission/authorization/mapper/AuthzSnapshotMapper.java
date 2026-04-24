package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.dto.RbacSnapshotResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuthzSnapshotMapper {

  List<RbacSnapshotResponse.ApiResource> selectApisByApp(@Param("appCode") String appCode);

  List<RbacSnapshotResponse.RoleApiBinding> selectRoleApiBindingsByApp(@Param("appCode") String appCode);
}


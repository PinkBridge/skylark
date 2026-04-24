package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.RbacSnapshotResponse;
import cn.skylark.permission.authorization.mapper.AuthzSnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AuthzSnapshotService {

  @Resource
  private AuthzSnapshotMapper mapper;

  public RbacSnapshotResponse snapshot(String appCode, long sinceVersion) {
    String code = StringUtils.hasText(appCode) ? appCode.trim() : null;

    List<RbacSnapshotResponse.ApiResource> apis = mapper.selectApisByApp(code);
    List<RbacSnapshotResponse.RoleApiBinding> bindings = mapper.selectRoleApiBindingsByApp(code);

    RbacSnapshotResponse resp = new RbacSnapshotResponse();
    // Use a simple time-based version. Clients treat it as opaque.
    resp.setVersion(System.currentTimeMillis());
    resp.setApis(apis);
    resp.setRoleApiBindings(bindings);
    return resp;
  }
}


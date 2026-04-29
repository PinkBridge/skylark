package cn.skylark.aiot_service.iot.mgmt.service;

import cn.skylark.datadomain.starter.context.DataDomainContext;
import org.springframework.http.HttpStatus;

public final class AiotDataDomainSupport {
  private AiotDataDomainSupport() {}

  public static Long requireTenantId() {
    Long tenantId = DataDomainContext.getTenantId();
    if (tenantId == null) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "tenant_id is required");
    }
    return tenantId;
  }

  public static Long currentOrgId() {
    return DataDomainContext.getOrgId();
  }
}


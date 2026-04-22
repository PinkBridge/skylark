package cn.skylark.datadomain.starter.resolve;

import cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO;
import org.springframework.security.core.Authentication;

public class NoOpDataScopeResolver implements DataScopeResolver {
  @Override
  public ResolvedDataScopeDTO resolve(Authentication authentication) {
    return new ResolvedDataScopeDTO();
  }
}

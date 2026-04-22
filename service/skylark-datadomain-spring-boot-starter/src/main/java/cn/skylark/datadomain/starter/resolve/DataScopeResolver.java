package cn.skylark.datadomain.starter.resolve;

import cn.skylark.datadomain.starter.dto.ResolvedDataScopeDTO;
import org.springframework.security.core.Authentication;

/**
 * Resolves {@link ResolvedDataScopeDTO} for the current HTTP request.
 */
public interface DataScopeResolver {

  /**
   * @param authentication non-null authenticated principal
   * @return resolved scope; never null (return empty DTO if unknown)
   */
  ResolvedDataScopeDTO resolve(Authentication authentication);
}

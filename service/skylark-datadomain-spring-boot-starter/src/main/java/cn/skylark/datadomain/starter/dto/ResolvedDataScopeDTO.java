package cn.skylark.datadomain.starter.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of resolving a user's data domain (union of all roles' data domains).
 * Semantics align with permission service: {@code allPlatform} wins; else {@code wholeTenant}
 * means no extra row filter; else combine {@code orgIds} and {@code selfOnly}.
 */
@Data
public class ResolvedDataScopeDTO {

  private String version = "1.0";

  private boolean allPlatform;
  private boolean wholeTenant;
  private boolean selfOnly;
  private List<Long> orgIds = new ArrayList<>();
  private boolean hasCustom;
  private List<Long> sourceDataDomainIds = new ArrayList<>();

  /**
   * Subject user id (for SELF row scope). Permission service should populate when resolving.
   */
  private Long userId;
}

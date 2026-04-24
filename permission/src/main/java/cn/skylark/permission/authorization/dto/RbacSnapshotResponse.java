package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * RBAC snapshot for business services to enforce API authorization locally.
 *
 * This DTO intentionally matches the shape expected by {@code skylark-authz-spring-boot-starter}.
 */
@Data
public class RbacSnapshotResponse {

  /**
   * Monotonic-ish version used by clients for incremental sync.
   * For now we use a timestamp; clients may treat it as opaque.
   */
  private long version;

  private List<ApiResource> apis = new ArrayList<>();
  private List<RoleApiBinding> roleApiBindings = new ArrayList<>();

  @Data
  public static class ApiResource {
    private Long apiId;
    private String method;
    private String pathPattern;
    private boolean enabled = true;
  }

  @Data
  public static class RoleApiBinding {
    private String roleName;
    private Long apiId;
    private boolean enabled = true;
  }
}


package cn.skylark.authz.starter.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * RBAC snapshot response contract returned by permission service.
 * <p>
 * This starter intentionally keeps the contract minimal and generic; the permission service
 * can evolve its schema as long as it can be mapped into this response.
 */
@Data
public class RbacSnapshotResponse {
  private long version;
  private List<ApiResource> apis = new ArrayList<>();
  private List<RoleApiBinding> roleApiBindings = new ArrayList<>();

  @Data
  public static class ApiResource {
    private Long apiId;
    private String method;       // GET/POST/PUT/DELETE/* (case-insensitive)
    private String pathPattern;  // ant-style pattern: /api/foo/**, /api/foo/*, /api/foo/{id}
    private boolean enabled = true;
  }

  @Data
  public static class RoleApiBinding {
    private String roleName; // e.g. ROLE_ADMIN or admin (caller decides convention)
    private Long apiId;
    private boolean enabled = true;
  }
}


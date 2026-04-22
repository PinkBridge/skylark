package cn.skylark.authz.starter.sync;

import cn.skylark.authz.starter.core.AuthzCache;
import cn.skylark.authz.starter.core.MethodPathPattern;
import cn.skylark.authz.starter.model.RbacSnapshotResponse;
import cn.skylark.authz.starter.permission.PermissionSnapshotClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Runs periodic RBAC snapshot sync and updates {@link AuthzCache}.
 */
@Slf4j
@RequiredArgsConstructor
public class AuthzSnapshotSyncJob {

  private final PermissionSnapshotClient client;
  private final AuthzCache cache;

  public void syncOnce() {
    long since = cache.getState().getVersion();
    RbacSnapshotResponse snapshot = client.fetchRbacSnapshot(since);
    if (snapshot == null) {
      return;
    }
    if (snapshot.getVersion() <= since) {
      return;
    }
    AuthzCache.State newState = buildState(snapshot);
    cache.replace(newState);
    log.info("Authz snapshot updated: {} -> {}, roles={}",
            since, snapshot.getVersion(), newState.getRoleToAllowed().size());
  }

  private static AuthzCache.State buildState(RbacSnapshotResponse snapshot) {
    Map<Long, RbacSnapshotResponse.ApiResource> apiById = new HashMap<>();
    for (RbacSnapshotResponse.ApiResource api : snapshot.getApis()) {
      if (api == null || api.getApiId() == null) {
        continue;
      }
      if (!api.isEnabled()) {
        continue;
      }
      if (api.getMethod() == null || api.getPathPattern() == null) {
        continue;
      }
      apiById.put(api.getApiId(), api);
    }

    Map<String, List<MethodPathPattern>> roleToAllowed = new HashMap<>();
    for (RbacSnapshotResponse.RoleApiBinding b : snapshot.getRoleApiBindings()) {
      if (b == null || !b.isEnabled()) {
        continue;
      }
      if (b.getRoleName() == null || b.getApiId() == null) {
        continue;
      }
      RbacSnapshotResponse.ApiResource api = apiById.get(b.getApiId());
      if (api == null) {
        continue;
      }
      String method = api.getMethod().trim().isEmpty() ? "*" : api.getMethod().trim().toUpperCase(Locale.ROOT);
      String pathPattern = api.getPathPattern().trim();
      roleToAllowed.computeIfAbsent(b.getRoleName(), k -> new ArrayList<>())
              .add(new MethodPathPattern(method, pathPattern));
    }

    return AuthzCache.build(snapshot.getVersion(), roleToAllowed);
  }
}


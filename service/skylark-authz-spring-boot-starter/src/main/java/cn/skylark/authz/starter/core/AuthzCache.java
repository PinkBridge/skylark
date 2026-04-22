package cn.skylark.authz.starter.core;

import lombok.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory RBAC cache for fast request-time checks.
 */
public class AuthzCache {

  private final AtomicReference<State> stateRef = new AtomicReference<>(State.empty());

  public State getState() {
    return stateRef.get();
  }

  public void replace(State newState) {
    if (newState == null) {
      return;
    }
    stateRef.set(newState);
  }

  @Value
  public static class State {
    long version;
    Map<String, List<MethodPathPattern>> roleToAllowed; // role -> patterns
    PathMatcher pathMatcher;

    public static State empty() {
      return new State(0L, Collections.emptyMap(), new AntPathMatcher());
    }

    public boolean isAllowed(Set<String> roles, String httpMethod, String path) {
      if (roles == null || roles.isEmpty()) {
        return false;
      }
      String m = httpMethod == null ? "" : httpMethod.toUpperCase(Locale.ROOT);
      for (String role : roles) {
        List<MethodPathPattern> patterns = roleToAllowed.get(role);
        if (patterns == null || patterns.isEmpty()) {
          continue;
        }
        for (MethodPathPattern p : patterns) {
          if (!"*".equals(p.getMethod()) && !p.getMethod().equals(m)) {
            continue;
          }
          if (pathMatcher.match(p.getPathPattern(), path)) {
            return true;
          }
        }
      }
      return false;
    }
  }

  public static State build(long version, Map<String, List<MethodPathPattern>> roleToAllowed) {
    Map<String, List<MethodPathPattern>> copy = new HashMap<>();
    for (Map.Entry<String, List<MethodPathPattern>> e : roleToAllowed.entrySet()) {
      copy.put(e.getKey(), Collections.unmodifiableList(new ArrayList<>(e.getValue())));
    }
    return new State(version, Collections.unmodifiableMap(copy), new AntPathMatcher());
  }
}


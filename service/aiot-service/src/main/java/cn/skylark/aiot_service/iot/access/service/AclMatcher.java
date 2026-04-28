package cn.skylark.aiot_service.iot.access.service;

import cn.skylark.aiot_service.iot.access.model.AclPolicyRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AclMatcher {
  public boolean isAllowed(List<AclPolicyRecord> candidates, String topic) {
    if (candidates == null || candidates.isEmpty()) {
      return false;
    }
    for (AclPolicyRecord item : candidates) {
      if (item == null) continue;
      if (item.getEnabled() != null && item.getEnabled() == 0) continue;
      if (!matches(item.getTopicPattern(), topic)) continue;
      String effect = item.getEffect() == null ? "" : item.getEffect().trim().toLowerCase();
      if ("allow".equals(effect)) return true;
      if ("deny".equals(effect)) return false;
    }
    return false;
  }

  private boolean matches(String pattern, String topic) {
    if (pattern == null || topic == null) return false;
    String p = pattern.trim();
    String t = topic.trim();
    if ("*".equals(p) || "#".equals(p)) return true;
    if (p.equals(t)) return true;
    if (p.endsWith("/#")) {
      String prefix = p.substring(0, p.length() - 2);
      return t.equals(prefix) || t.startsWith(prefix + "/");
    }
    return false;
  }
}


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
    String[] pSeg = p.split("/", -1);
    String[] tSeg = t.split("/", -1);
    int i = 0;
    int j = 0;
    while (i < pSeg.length && j < tSeg.length) {
      String token = pSeg[i];
      if ("#".equals(token)) {
        // MQTT '#': match current and all remaining levels.
        return i == pSeg.length - 1;
      }
      if ("+".equals(token)) {
        // MQTT '+': match exactly one level.
        i++;
        j++;
        continue;
      }
      if (!token.equals(tSeg[j])) {
        return false;
      }
      i++;
      j++;
    }
    if (i == pSeg.length && j == tSeg.length) {
      return true;
    }
    if (i == pSeg.length - 1 && "#".equals(pSeg[i])) {
      return true;
    }
    return false;
  }
}


package cn.skylark.aiot_service.iot.appint;

import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.appint.model.OutboundDispatchRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.io.IOException;

final class OutboundSubscriptionMatcher {
  private OutboundSubscriptionMatcher() {}

  static boolean matchOrg(OutboundDispatchRow row, NormalizedEvent event) {
    Long subOrg = row.getSubscriptionOrgId();
    if (subOrg == null) {
      return true;
    }
    return event.getOrgId() != null && subOrg.equals(event.getOrgId());
  }

  static boolean matchEventType(String eventTypesJson, String eventType, ObjectMapper mapper) {
    if (!StringUtils.hasText(eventTypesJson) || !StringUtils.hasText(eventType)) {
      return false;
    }
    try {
      JsonNode root = mapper.readTree(eventTypesJson);
      if (!root.isArray()) {
        return false;
      }
      for (JsonNode n : root) {
        if (n != null && n.isTextual() && eventType.equals(n.asText())) {
          return true;
        }
      }
      return false;
    } catch (IOException e) {
      return false;
    }
  }

  static boolean matchFilter(String filterJson, NormalizedEvent event, ObjectMapper mapper) {
    if (!StringUtils.hasText(filterJson)) {
      return true;
    }
    try {
      JsonNode f = mapper.readTree(filterJson);
      if (f == null || f.isNull() || (f.isObject() && !f.fieldNames().hasNext())) {
        return true;
      }
      String pk = subject(event, "productKey");
      String dk = subject(event, "deviceKey");
      String pkPrefix = text(f, "productKeyPrefix");
      if (StringUtils.hasText(pkPrefix) && (pk == null || !pk.startsWith(pkPrefix))) {
        return false;
      }
      String dkPrefix = text(f, "deviceKeyPrefix");
      if (StringUtils.hasText(dkPrefix) && (dk == null || !dk.startsWith(dkPrefix))) {
        return false;
      }
      String pkExact = text(f, "productKey");
      if (StringUtils.hasText(pkExact) && (pk == null || !pkExact.equals(pk))) {
        return false;
      }
      String dkExact = text(f, "deviceKey");
      if (StringUtils.hasText(dkExact) && (dk == null || !dkExact.equals(dk))) {
        return false;
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private static String text(JsonNode f, String field) {
    JsonNode n = f.get(field);
    if (n == null || !n.isTextual()) {
      return null;
    }
    String t = n.asText();
    return StringUtils.hasText(t) ? t : null;
  }

  private static String subject(NormalizedEvent event, String key) {
    if (event.getSubject() == null) {
      return null;
    }
    return event.getSubject().get(key);
  }
}

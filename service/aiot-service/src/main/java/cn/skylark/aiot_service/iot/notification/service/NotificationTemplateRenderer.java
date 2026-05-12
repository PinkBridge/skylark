package cn.skylark.aiot_service.iot.notification.service;

import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Mustache-style rendering: {@code {{path.to.value}}} against a tree built from {@link NormalizedEvent}.
 */
@Component
public class NotificationTemplateRenderer {
  private static final Pattern TAG = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*\\}}");

  private final ObjectMapper objectMapper;

  public NotificationTemplateRenderer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String render(String template, NormalizedEvent event) {
    if (!StringUtils.hasText(template)) {
      return "";
    }
    Map<String, Object> root = buildRoot(event);
    Matcher m = TAG.matcher(template);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String path = m.group(1);
      String value = formatLeaf(resolvePath(root, path));
      m.appendReplacement(sb, Matcher.quoteReplacement(value));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private Map<String, Object> buildRoot(NormalizedEvent event) {
    Map<String, Object> root = new LinkedHashMap<String, Object>();
    if (event == null) {
      return root;
    }
    root.put("eventId", event.getEventId());
    root.put("eventType", event.getEventType());
    root.put("occurredAt", event.getOccurredAt());
    root.put("tenantId", event.getTenantId());
    root.put("orgId", event.getOrgId());
    root.put("source", event.getSource());
    if (event.getSubject() != null) {
      root.put("subject", new LinkedHashMap<String, Object>(event.getSubject()));
    }
    if (event.getData() != null) {
      root.put("data", event.getData());
    }
    return root;
  }

  private static Object resolvePath(Map<String, Object> root, String path) {
    if (root == null || !StringUtils.hasText(path)) {
      return null;
    }
    String[] parts = path.split("\\.");
    Object cur = root;
    for (String p : parts) {
      if (cur == null) {
        return null;
      }
      if (cur instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) cur;
        cur = map.get(p);
      } else {
        return null;
      }
    }
    return cur;
  }

  private String formatLeaf(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof Number || value instanceof Boolean) {
      return String.valueOf(value);
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return String.valueOf(value);
    }
  }
}

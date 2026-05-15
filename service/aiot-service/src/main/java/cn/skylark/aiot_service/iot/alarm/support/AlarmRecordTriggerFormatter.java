package cn.skylark.aiot_service.iot.alarm.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

/**
 * Derives human-readable trigger condition / value from persisted {@code evidenceJson}
 * (same shape as {@code AlarmEvaluatorService} evidence maps).
 */
public final class AlarmRecordTriggerFormatter {
  private AlarmRecordTriggerFormatter() {
  }

  public static String conditionSummary(ObjectMapper om, String evidenceJson) {
    JsonNode o = parseRoot(om, evidenceJson);
    if (o == null || !o.isObject()) {
      return "";
    }
    String st = upper(o.path("sourceType").asText("PROPERTY"));
    if ("EVENT".equals(st)) {
      String et = trimToEmpty(o.path("eventType").asText(null));
      String ei = trimToEmpty(o.path("eventIdentifier").asText(null));
      if (StringUtils.hasText(ei)) {
        return (StringUtils.hasText(et) ? et : "") + " · " + ei;
      }
      return StringUtils.hasText(et) ? et : "";
    }
    String pk = trimToEmpty(o.path("propertyKey").asText(null));
    String op = upper(trimToEmpty(o.path("operator").asText(null)));
    JsonNode th = o.get("threshold");
    if (!StringUtils.hasText(pk) && !StringUtils.hasText(op)) {
      return "";
    }
    if ("BETWEEN".equals(op) && th != null && th.isObject()) {
      if (th.hasNonNull("min") && th.hasNonNull("max")) {
        return pk + " BETWEEN " + th.get("min").asText() + ".." + th.get("max").asText();
      }
    }
    if (th != null && th.isObject() && th.has("value") && !th.get("value").isNull()) {
      return (StringUtils.hasText(pk) ? pk : "") + " " + op + " " + th.get("value").asText("").trim();
    }
    String base = (StringUtils.hasText(pk) ? pk : "") + " " + op;
    return base.trim();
  }

  public static String valueDisplay(ObjectMapper om, String evidenceJson, String lastEventType, String lastEventId) {
    JsonNode o = parseRoot(om, evidenceJson);
    if (o == null || !o.isObject()) {
      return fallbackEventHint(lastEventType, lastEventId);
    }
    String st = upper(o.path("sourceType").asText("PROPERTY"));
    if ("EVENT".equals(st)) {
      String lt = trimToEmpty(lastEventType);
      String lid = trimToEmpty(lastEventId);
      StringBuilder sb = new StringBuilder();
      if (StringUtils.hasText(lt)) {
        sb.append(lt);
      }
      if (StringUtils.hasText(lid)) {
        if (sb.length() > 0) {
          sb.append(" · ");
        }
        sb.append("id:");
        sb.append(lid.length() > 24 ? lid.substring(0, 21) + "\u2026" : lid);
      }
      if (sb.length() > 0) {
        return sb.toString();
      }
      String et = trimToEmpty(o.path("eventType").asText(null));
      return StringUtils.hasText(et) ? et : "";
    }
    JsonNode v = o.get("value");
    if (v == null || v.isNull() || v.isMissingNode()) {
      return "";
    }
    if (v.isNumber()) {
      return v.asText();
    }
    return trimToEmpty(v.asText(null));
  }

  private static String fallbackEventHint(String lastEventType, String lastEventId) {
    String lt = trimToEmpty(lastEventType);
    String lid = trimToEmpty(lastEventId);
    StringBuilder sb = new StringBuilder();
    if (StringUtils.hasText(lt)) {
      sb.append(lt);
    }
    if (StringUtils.hasText(lid)) {
      if (sb.length() > 0) {
        sb.append(" · ");
      }
      sb.append("id:");
      sb.append(lid.length() > 24 ? lid.substring(0, 21) + "\u2026" : lid);
    }
    return sb.toString();
  }

  private static JsonNode parseRoot(ObjectMapper om, String evidenceJson) {
    if (om == null || !StringUtils.hasText(evidenceJson)) {
      return null;
    }
    try {
      return om.readTree(evidenceJson);
    } catch (Exception e) {
      return null;
    }
  }

  private static String upper(String s) {
    return s == null ? "" : s.trim().toUpperCase();
  }

  private static String trimToEmpty(String s) {
    if (!StringUtils.hasText(s)) {
      return "";
    }
    return s.trim();
  }
}

package cn.skylark.aiot_service.iot.appint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public final class ConfigSecretMasker {
  private ConfigSecretMasker() {}

  public static String maskJson(String configJson, ObjectMapper mapper) {
    if (!StringUtils.hasText(configJson)) {
      return configJson;
    }
    try {
      JsonNode root = mapper.readTree(configJson);
      if (!root.isObject()) {
        return configJson;
      }
      ObjectNode copy = (ObjectNode) mapper.readTree(configJson);
      Iterator<Map.Entry<String, JsonNode>> it = copy.fields();
      while (it.hasNext()) {
        Map.Entry<String, JsonNode> e = it.next();
        String k = e.getKey();
        if (isSecretKey(k)) {
          copy.put(k, "***");
        }
      }
      return mapper.writeValueAsString(copy);
    } catch (Exception e) {
      return "{}";
    }
  }

  private static boolean isSecretKey(String key) {
    if (key == null) {
      return false;
    }
    String lower = key.toLowerCase(Locale.ROOT);
    return lower.contains("secret") || lower.contains("password") || lower.contains("token")
        || lower.contains("credential") || "signingsecret".equals(lower.replace("_", ""));
  }
}

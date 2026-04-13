package cn.skylark.permission.authorization.support;

import cn.skylark.permission.authorization.entity.SysMenu;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Resolves {@link SysMenu#getNameI18n()} JSON ({@code zh} / {@code en}) using request {@link Locale}.
 */
@Slf4j
@Component
public class MenuI18nResolver {

  private static final String KEY_ZH = "zh";
  private static final String KEY_EN = "en";

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Display name for the given locale; falls back to {@link SysMenu#getName()} when JSON is missing or invalid.
   */
  public String resolveDisplayName(SysMenu menu, Locale locale) {
    if (menu == null) {
      return null;
    }
    String raw = menu.getNameI18n();
    if (!StringUtils.hasText(raw)) {
      return menu.getName();
    }
    try {
      JsonNode node = objectMapper.readTree(raw);
      if (!node.isObject()) {
        return menu.getName();
      }
      if (locale != null) {
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (StringUtils.hasText(country)) {
          String withCountry = lang + "_" + country;
          String v = textIfPresent(node, withCountry);
          if (v != null) {
            return v;
          }
        }
        String v = textIfPresent(node, lang);
        if (v != null) {
          return v;
        }
      }
      String en = textIfPresent(node, KEY_EN);
      if (en != null) {
        return en;
      }
      String zh = textIfPresent(node, KEY_ZH);
      if (zh != null) {
        return zh;
      }
    } catch (Exception e) {
      log.warn("Invalid menu name_i18n for menu id {}: {}", menu.getId(), e.getMessage());
    }
    return menu.getName();
  }

  private static String textIfPresent(JsonNode node, String field) {
    if (!node.has(field) || !node.get(field).isTextual()) {
      return null;
    }
    String v = node.get(field).asText();
    return StringUtils.hasText(v) ? v : null;
  }

  /**
   * Default JSON for a new menu when only {@link SysMenu#getName()} is set.
   */
  public String defaultNameI18nJson(String name) {
    if (!StringUtils.hasText(name)) {
      return null;
    }
    try {
      return objectMapper.createObjectNode()
          .put(KEY_ZH, name.trim())
          .put(KEY_EN, name.trim())
          .toString();
    } catch (Exception e) {
      return null;
    }
  }
}

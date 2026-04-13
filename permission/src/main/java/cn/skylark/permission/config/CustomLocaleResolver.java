package cn.skylark.permission.config;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 自定义Locale解析器
 * 从请求头 X-Language 或 Accept-Language 获取语言设置
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
public class CustomLocaleResolver implements LocaleResolver {

  private static final String LANGUAGE_HEADER = "X-Language";
  private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
  private static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    // 优先从自定义请求头 X-Language 获取
    String language = request.getHeader(LANGUAGE_HEADER);
    
    if (StringUtils.hasText(language)) {
      return parseLocale(language);
    }

    // 其次从标准请求头 Accept-Language 获取
    String acceptLanguage = request.getHeader(ACCEPT_LANGUAGE_HEADER);
    if (StringUtils.hasText(acceptLanguage)) {
      // 解析 Accept-Language，取第一个语言
      String[] languages = acceptLanguage.split(",");
      if (languages.length > 0) {
        String lang = languages[0].trim().split(";")[0].trim();
        return parseLocale(lang);
      }
    }

    // 默认返回中文
    return DEFAULT_LOCALE;
  }

  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    // 如果需要支持设置Locale，可以在这里实现
    // 例如：将Locale存储到Session或Cookie中
  }

  /**
   * 解析语言代码为Locale对象
   * 支持格式：en, zh-CN, zh_CN, en-US, en_US 等
   */
  private Locale parseLocale(String language) {
    if (!StringUtils.hasText(language)) {
      return DEFAULT_LOCALE;
    }

    language = language.trim();
    
    // 处理下划线分隔符（如：zh_CN）
    if (language.contains("_")) {
      String[] parts = language.split("_");
      if (parts.length == 2) {
        return new Locale(parts[0], parts[1]);
      }
    }
    
    // 处理连字符分隔符（如：zh-CN）
    if (language.contains("-")) {
      String[] parts = language.split("-");
      if (parts.length == 2) {
        return new Locale(parts[0], parts[1]);
      }
    }
    
    // 只有语言代码（如：en, zh）
    return new Locale(language);
  }
}


package cn.skylark.permission.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * 国际化工具类
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Component
public class I18nUtil {

  @Resource
  private MessageSource messageSource;

  /**
   * 获取国际化消息
   *
   * @param code 消息代码
   * @return 国际化消息
   */
  public String getMessage(String code) {
    return getMessage(code, null);
  }

  /**
   * 获取国际化消息
   *
   * @param code 消息代码
   * @param args 参数
   * @return 国际化消息
   */
  public String getMessage(String code, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage(code, args, locale);
  }

  /**
   * 获取国际化消息，如果不存在则返回默认消息
   *
   * @param code          消息代码
   * @param defaultMessage 默认消息
   * @param args          参数
   * @return 国际化消息
   */
  public String getMessage(String code, String defaultMessage, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage(code, args, defaultMessage, locale);
  }
}


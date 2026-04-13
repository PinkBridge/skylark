package cn.skylark.permission.utils;


import lombok.Data;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Locale;

/**
 * @author yaomianwei
 * @since 17:35 2025/10/31
 **/
@Data
public class Ret<T> {
  /**
   *
   */
  private int code;
  private T data;
  private String message;

  // 静态MessageSource，用于国际化
  private static MessageSourceAccessor messageSourceAccessor;

  /**
   * 设置MessageSource（由Spring容器注入）
   */
  public static void setMessageSource(MessageSource messageSource) {
    Ret.messageSourceAccessor = new MessageSourceAccessor(messageSource);
  }

  public static <T> Ret<T> data(T data) {
    Ret<T> ret = new Ret<>();
    ret.setCode(200);
    ret.setData(data);
    ret.setMessage(getI18nMessage("success", "success"));
    return ret;
  }

  public static <T> Ret<T> ok() {
    Ret<T> ret = new Ret<>();
    ret.setCode(200);
    ret.setData(null);
    ret.setMessage(getI18nMessage("success", "success"));
    return ret;
  }

  public static <T> Ret<T> resp(Integer code, T data, String message) {
    Ret<T> ret = new Ret<>();
    ret.setCode(code);
    ret.setData(data);
    ret.setMessage(getI18nMessage(message, message));
    return ret;
  }

  public static <T> Ret<T> fail(Integer failCode, String failMessage) {
    Ret<T> ret = new Ret<>();
    ret.setCode(failCode);
    ret.setData(null);
    ret.setMessage(getI18nMessage(failMessage, failMessage));
    return ret;
  }

  /**
   * 失败响应，支持国际化消息代码
   *
   * @param failCode    错误代码
   * @param messageCode 消息代码
   * @return Ret对象
   */
  public static <T> Ret<T> fail(Integer failCode, String messageCode, Object... args) {
    Ret<T> ret = new Ret<>();
    ret.setCode(failCode);
    ret.setData(null);
    ret.setMessage(getI18nMessage(messageCode, messageCode, args));
    return ret;
  }

  /**
   * 获取国际化消息
   */
  private static String getI18nMessage(String code, String defaultMessage, Object... args) {
    if (messageSourceAccessor != null) {
      try {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSourceAccessor.getMessage(code, args, defaultMessage, locale);
      } catch (Exception e) {
        // 如果获取失败，返回默认消息
        return defaultMessage;
      }
    }
    return defaultMessage;
  }
}

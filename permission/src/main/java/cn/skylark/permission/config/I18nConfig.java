package cn.skylark.permission.config;

import cn.skylark.permission.utils.Ret;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 国际化配置
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

  @Resource
  private MessageSource messageSource;

  @Resource
  private LocalDateTimeConverter localDateTimeConverter;

  @Bean
  public LocaleResolver localeResolver() {
    return new CustomLocaleResolver();
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(localDateTimeConverter);
  }

  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    // 支持通过URL参数切换语言，如：?lang=en
    interceptor.setParamName("lang");
    return interceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
  }

  /**
   * 初始化Ret类的MessageSource
   */
  @PostConstruct
  public void initRetMessageSource() {
    Ret.setMessageSource(messageSource);
  }
}


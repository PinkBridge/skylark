package cn.skylark.web.exception;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@ConditionalOnProperty(prefix = "skylark.web.exception", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SkylarkWebExceptionProperties.class)
@Import(SkylarkGlobalExceptionHandler.class)
public class SkylarkWebExceptionAutoConfiguration {

  @Bean
  public FilterRegistrationBean<SkylarkExceptionLoggingFilter> skylarkExceptionLoggingFilter(SkylarkWebExceptionProperties props) {
    FilterRegistrationBean<SkylarkExceptionLoggingFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new SkylarkExceptionLoggingFilter(props));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }
}


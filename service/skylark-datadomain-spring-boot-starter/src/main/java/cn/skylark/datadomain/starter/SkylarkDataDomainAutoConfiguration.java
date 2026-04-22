package cn.skylark.datadomain.starter;

import cn.skylark.datadomain.starter.permission.ServiceTokenProvider;
import cn.skylark.datadomain.starter.resolve.CachedPermissionRemoteDataScopeResolver;
import cn.skylark.datadomain.starter.resolve.DataScopeResolver;
import cn.skylark.datadomain.starter.resolve.NoOpDataScopeResolver;
import cn.skylark.datadomain.starter.web.DataScopeHandlerInterceptor;
import cn.skylark.datadomain.starter.web.TenantHeaderFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(SkylarkDataDomainProperties.class)
@ConditionalOnProperty(prefix = "skylark.datadomain", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class SkylarkDataDomainAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Clock skylarkDataDomainClock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnMissingBean
  public RestTemplate skylarkDataDomainRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  public FilterRegistrationBean<TenantHeaderFilter> skylarkTenantHeaderFilterRegistration(SkylarkDataDomainProperties props) {
    FilterRegistrationBean<TenantHeaderFilter> reg = new FilterRegistrationBean<>(new TenantHeaderFilter(props));
    reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
    return reg;
  }

  @Bean
  @ConditionalOnProperty(prefix = "skylark.datadomain", name = "resolve-data-scope", havingValue = "true")
  @ConditionalOnMissingBean
  public ServiceTokenProvider skylarkDataDomainServiceTokenProvider(RestTemplate skylarkDataDomainRestTemplate,
                                                                    SkylarkDataDomainProperties props,
                                                                    Clock skylarkDataDomainClock) {
    validateRemote(props);
    return new ServiceTokenProvider(skylarkDataDomainRestTemplate, props, skylarkDataDomainClock);
  }

  @Bean
  @ConditionalOnMissingBean
  public DataScopeResolver skylarkDataScopeResolver(SkylarkDataDomainProperties props,
                                                     RestTemplate skylarkDataDomainRestTemplate,
                                                     Clock skylarkDataDomainClock,
                                                     ObjectProvider<ServiceTokenProvider> tokenProvider) {
    if (!props.isResolveDataScope()) {
      return new NoOpDataScopeResolver();
    }
    validateRemote(props);
    ServiceTokenProvider tp = tokenProvider.getIfAvailable();
    if (tp == null) {
      tp = new ServiceTokenProvider(skylarkDataDomainRestTemplate, props, skylarkDataDomainClock);
    }
    return new CachedPermissionRemoteDataScopeResolver(
        skylarkDataDomainRestTemplate, props, tp, skylarkDataDomainClock);
  }

  @Bean
  public WebMvcConfigurer skylarkDataDomainWebMvcConfigurer(SkylarkDataDomainProperties props,
                                                            DataScopeResolver skylarkDataScopeResolver) {
    return new WebMvcConfigurer() {
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DataScopeHandlerInterceptor(props, skylarkDataScopeResolver))
            .addPathPatterns("/**");
      }
    };
  }

  private static void validateRemote(SkylarkDataDomainProperties props) {
    if (!StringUtils.hasText(props.getPermissionBaseUrl())) {
      throw new IllegalStateException("skylark.datadomain.permission-base-url is required when resolve-data-scope=true");
    }
    if (!StringUtils.hasText(props.getClientId()) || !StringUtils.hasText(props.getClientSecret())) {
      throw new IllegalStateException("skylark.datadomain.client-id and client-secret are required when resolve-data-scope=true");
    }
  }
}

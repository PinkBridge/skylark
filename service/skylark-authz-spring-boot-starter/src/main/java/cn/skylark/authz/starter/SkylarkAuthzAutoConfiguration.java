package cn.skylark.authz.starter;

import cn.skylark.authz.starter.core.AuthzCache;
import cn.skylark.authz.starter.permission.PermissionSnapshotClient;
import cn.skylark.authz.starter.permission.ServiceTokenProvider;
import cn.skylark.authz.starter.sync.AuthzSnapshotSyncJob;
import cn.skylark.authz.starter.web.AuthzHandlerInterceptor;
import cn.skylark.authz.starter.web.JwtClaimsAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(SkylarkAuthzProperties.class)
@ConditionalOnProperty(prefix = "skylark.authz", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({WebMvcConfigurer.class, RestTemplate.class})
public class SkylarkAuthzAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Clock skylarkAuthzClock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnMissingBean
  public AuthzCache skylarkAuthzCache() {
    return new AuthzCache();
  }

  @Bean
  @ConditionalOnMissingBean
  public RestTemplate skylarkAuthzRestTemplate(ObjectProvider<List<ClientHttpRequestInterceptor>> interceptorsProvider) {
    RestTemplate rt = new RestTemplate();
    List<ClientHttpRequestInterceptor> interceptors = interceptorsProvider.getIfAvailable();
    if (interceptors != null && !interceptors.isEmpty()) {
      rt.setInterceptors(new ArrayList<>(interceptors));
    }
    return rt;
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceTokenProvider skylarkServiceTokenProvider(RestTemplate skylarkAuthzRestTemplate,
                                                          SkylarkAuthzProperties props,
                                                          Clock skylarkAuthzClock) {
    validate(props);
    return new ServiceTokenProvider(skylarkAuthzRestTemplate, props, skylarkAuthzClock);
  }

  @Bean
  @ConditionalOnMissingBean
  public PermissionSnapshotClient skylarkPermissionSnapshotClient(RestTemplate skylarkAuthzRestTemplate,
                                                                  SkylarkAuthzProperties props,
                                                                  ServiceTokenProvider tokenProvider) {
    validate(props);
    return new PermissionSnapshotClient(skylarkAuthzRestTemplate, props, tokenProvider);
  }

  @Bean
  @ConditionalOnMissingBean
  public AuthzSnapshotSyncJob skylarkAuthzSnapshotSyncJob(PermissionSnapshotClient client, AuthzCache cache) {
    return new AuthzSnapshotSyncJob(client, cache);
  }

  @Bean
  @ConditionalOnMissingBean
  public AuthzHandlerInterceptor skylarkAuthzHandlerInterceptor(SkylarkAuthzProperties props, AuthzCache cache) {
    return new AuthzHandlerInterceptor(props, cache);
  }

  @Bean
  @ConditionalOnMissingBean
  public WebMvcConfigurer skylarkAuthzWebMvcConfigurer(AuthzHandlerInterceptor interceptor) {
    return new WebMvcConfigurer() {
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
      }
    };
  }

  @Bean
  @ConditionalOnProperty(prefix = "skylark.authz.jwt", name = "enabled", havingValue = "true")
  public FilterRegistrationBean<JwtClaimsAuthenticationFilter> skylarkJwtClaimsAuthenticationFilterRegistration(SkylarkAuthzProperties props) {
    FilterRegistrationBean<JwtClaimsAuthenticationFilter> reg =
        new FilterRegistrationBean<>(new JwtClaimsAuthenticationFilter(props));
    // Run early so downstream interceptors (authz/datadomain) can read SecurityContext.
    reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return reg;
  }

  @Configuration
  static class SyncScheduler {
    private final AuthzSnapshotSyncJob job;
    private final SkylarkAuthzProperties props;

    SyncScheduler(AuthzSnapshotSyncJob job, SkylarkAuthzProperties props) {
      this.job = job;
      this.props = props;
    }

    @Scheduled(fixedDelayString = "${skylark.authz.sync-fixed-delay-ms:30000}")
    public void sync() {
      if (!props.isEnabled()) {
        return;
      }
      try {
        job.syncOnce();
      } catch (Exception e) {
        log.warn("Authz snapshot sync failed: {}", e.getMessage());
        log.debug("Authz snapshot sync exception", e);
      }
    }
  }

  private static void validate(SkylarkAuthzProperties props) {
    if (!StringUtils.hasText(props.getPermissionBaseUrl())) {
      throw new IllegalStateException("skylark.authz.permission-base-url must be set");
    }
    if (!StringUtils.hasText(props.getAppCode())) {
      throw new IllegalStateException("skylark.authz.app-code must be set");
    }
    if (!StringUtils.hasText(props.getClientId()) || !StringUtils.hasText(props.getClientSecret())) {
      throw new IllegalStateException("skylark.authz.client-id and skylark.authz.client-secret must be set");
    }
  }
}


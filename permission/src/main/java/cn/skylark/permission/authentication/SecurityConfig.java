package cn.skylark.permission.authentication;

import cn.skylark.permission.authentication.filter.AllPlatformDataScopeWebFilter;
import cn.skylark.permission.authentication.filter.JwtAuthenticationFilter;
import cn.skylark.permission.authentication.filter.LoginLogFilter;
import cn.skylark.permission.authentication.filter.OperationLogFilter;
import cn.skylark.permission.authentication.filter.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;

/**
 * @author yaomianwei
 * @since 15:47 2025/11/1
 **/
@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Resource
  private UserDetailsService customUserDetailsService;

  @Resource
  private LogoutSuccessHandler customLogoutSuccessHandler;

  @Resource
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Resource
  private TenantFilter tenantFilter;

  @Resource
  private LoginLogFilter loginLogFilter;

  @Resource
  private OperationLogFilter operationLogFilter;

  @Resource
  private AllPlatformDataScopeWebFilter allPlatformDataScopeWebFilter;

  @Resource
  private AccessDeniedHandler customAccessDeniedHandler;

  @Resource
  private AuthenticationEntryPoint customAuthenticationEntryPoint;

  private static final String API_PREFIX = "/api/**";

  @Override
  @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public static NoOpPasswordEncoder passwordEncoder() {
    return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // use user information in database for authentication
    auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
            .exceptionHandling()
            .defaultAccessDeniedHandlerFor(customAccessDeniedHandler, new AntPathRequestMatcher(API_PREFIX))
            .defaultAuthenticationEntryPointFor(customAuthenticationEntryPoint, new AntPathRequestMatcher(API_PREFIX))
            .and()
            .authorizeRequests()
            .antMatchers("/oauth/token").permitAll()
            .antMatchers("/oauth/authorize").authenticated()
            .antMatchers("/oauth/**").permitAll()
            .antMatchers("/api/permission/tenants/domain/**").permitAll()
            .antMatchers("/api/permission/platform-init/**").permitAll()
            // Init wizard uploads logo before login.
            .antMatchers(HttpMethod.POST, "/api/permission/resources/upload").permitAll()
            // 登录前页面（如 /welcome）展示租户 logo：<img> 无法带 Token，需匿名可读已上传文件
            .antMatchers(HttpMethod.GET, "/api/permission/resources/preview/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/permission/resources/download/**").permitAll()
            .anyRequest().access("@rbacService.hasPermission(request,authentication)")
            .and()
            .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loginLogFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(allPlatformDataScopeWebFilter, JwtAuthenticationFilter.class)
            .addFilterAfter(operationLogFilter, AllPlatformDataScopeWebFilter.class)
            .formLogin()
            .and()
            .logout()
            .logoutUrl("/oauth/logout")
            .clearAuthentication(true).invalidateHttpSession(true)
            .logoutSuccessHandler(customLogoutSuccessHandler)
            .permitAll();
  }
}

package cn.skylark.business_service.security;

import cn.skylark.authz.starter.SkylarkAuthzProperties;
import cn.skylark.authz.starter.web.JwtClaimsAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final SkylarkAuthzProperties authzProperties;

  public SecurityConfig(SkylarkAuthzProperties authzProperties) {
    this.authzProperties = authzProperties;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .exceptionHandling()
        .authenticationEntryPoint((request, response, authException) -> {
          response.setStatus(401);
          response.setCharacterEncoding(StandardCharsets.UTF_8.name());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.getWriter().write("{\"code\":401,\"data\":null,\"message\":\"unauthorized\"}");
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          response.setStatus(403);
          response.setCharacterEncoding(StandardCharsets.UTF_8.name());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.getWriter().write("{\"code\":403,\"data\":null,\"message\":\"forbidden\"}");
        })
        .and()
        // Pure API service: do not create server-side sessions.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        // Parse permission-issued JWT and populate SecurityContext before auth checks.
        .addFilterBefore(new JwtClaimsAuthenticationFilter(authzProperties), UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .antMatchers("/actuator/**").permitAll()
        .antMatchers("/api/business-service/demo/**").permitAll()
        .anyRequest().authenticated();
  }
}


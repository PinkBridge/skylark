package cn.skylark.demo.security;

import cn.skylark.authz.starter.SkylarkAuthzProperties;
import cn.skylark.authz.starter.web.JwtClaimsAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilterBefore(new JwtClaimsAuthenticationFilter(authzProperties), UsernamePasswordAuthenticationFilter.class)
        .authorizeRequests()
        .antMatchers("/actuator/**").permitAll()
        .antMatchers("/api/business-service/demo/**").permitAll()
        .anyRequest().authenticated();
  }
}


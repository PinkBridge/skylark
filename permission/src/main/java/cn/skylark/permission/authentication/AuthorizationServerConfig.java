package cn.skylark.permission.authentication;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;

/**
 * OAuth2 AuthorizationServerConfig
 *
 * @author yaomianwei
 * @since 14:55 2025/11/1
 **/
@Configuration
@SuppressWarnings("deprecation")
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  @Resource
  private AuthenticationManager authenticationManager;
  @Resource
  private OauthConfig oauthConfig;
  @Resource
  private DataSource dataSource;
  @Resource
  private UserDetailsService userDetailsService;
  @Resource
  private TokenEnhancer customClaimsTokenEnhancer;

  @Bean
  public AuthorizationCodeServices authorizationCodeServices() {
    // use datasource here
    return new JdbcAuthorizationCodeServices(dataSource);
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    // use jwt as access_toke
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(oauthConfig.getSigningKey());
    return converter;
  }

  @Bean
  public JwtTokenStore jwtTokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    TokenEnhancerChain chain = new TokenEnhancerChain();
    chain.setTokenEnhancers(Arrays.asList(customClaimsTokenEnhancer, accessTokenConverter()));
    endpoints.authenticationManager(authenticationManager)
            .tokenStore(jwtTokenStore())
            .accessTokenConverter(accessTokenConverter())
            .tokenEnhancer(chain)
            .authorizationCodeServices(authorizationCodeServices())
            // reload user details when refreshing the token
            .userDetailsService(userDetailsService)
            .reuseRefreshTokens(false)
    ;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
    oauthServer.checkTokenAccess("isAuthenticated()").allowFormAuthenticationForClients();
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.jdbc(dataSource);
  }
}

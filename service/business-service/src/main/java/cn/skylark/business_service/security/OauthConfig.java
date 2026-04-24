package cn.skylark.business_service.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth")
@Data
public class OauthConfig {
  private String signingKey;
}


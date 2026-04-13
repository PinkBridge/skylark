package cn.skylark.permission.authentication;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author yaomianwei
 * @since 16:02 2025/11/2
 **/
@Configuration
@ConfigurationProperties(prefix = "oauth")
@Data
public class OauthConfig {
  private String signingKey;
}

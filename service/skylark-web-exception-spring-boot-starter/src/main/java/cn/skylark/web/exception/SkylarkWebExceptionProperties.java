package cn.skylark.web.exception;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "skylark.web.exception")
public class SkylarkWebExceptionProperties {
  /**
   * Enable global exception handler.
   */
  private boolean enabled = true;

  /**
   * Whether to include exception stack trace in logs for 4xx errors.
   */
  private boolean logBadRequestStacktrace = true;
}


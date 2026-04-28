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

  /**
   * Whether to log stack trace for uncaught exceptions at filter level.
   * This is a safety net when exceptions are converted into error dispatch without logs.
   */
  private boolean logUncaughtStacktrace = true;
}


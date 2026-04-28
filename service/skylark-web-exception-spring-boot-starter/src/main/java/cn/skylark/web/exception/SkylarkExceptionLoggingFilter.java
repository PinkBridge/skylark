package cn.skylark.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Log uncaught exceptions at the filter boundary.
 *
 * Some exception flows (especially during error dispatch) may not print stack traces to stdout.
 * This filter ensures we always see the original exception in logs.
 */
@Slf4j
public class SkylarkExceptionLoggingFilter extends OncePerRequestFilter {

  private final SkylarkWebExceptionProperties props;

  public SkylarkExceptionLoggingFilter(SkylarkWebExceptionProperties props) {
    this.props = props;
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);

      Throwable error = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
      if (error != null) {
        if (props.isLogUncaughtStacktrace()) {
          log.error("Error dispatch exception: {} {} -> {}", request.getMethod(), request.getRequestURI(), String.valueOf(error), error);
        } else {
          log.error("Error dispatch exception: {} {} -> {}", request.getMethod(), request.getRequestURI(), String.valueOf(error));
        }
      } else if (response.getStatus() >= 500) {
        // In some flows container calls sendError(...) without an exception object.
        log.error("Server error without exception object: {} {} -> status={}", request.getMethod(), request.getRequestURI(), response.getStatus());
      }
    } catch (Throwable t) {
      if (props.isLogUncaughtStacktrace()) {
        log.error("Uncaught exception: {} {}", request.getMethod(), request.getRequestURI(), t);
      } else {
        log.error("Uncaught exception: {} {} -> {}", request.getMethod(), request.getRequestURI(), String.valueOf(t));
      }
      throw t;
    }
  }
}


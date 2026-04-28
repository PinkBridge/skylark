package cn.skylark.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.DispatcherType;
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

      // If we are in ERROR dispatch, always log servlet error attributes.
      if (request.getDispatcherType() == DispatcherType.ERROR) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object servletName = request.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME);
        Throwable error = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object errorType = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE);

        if (props.isLogUncaughtStacktrace() && error != null) {
          log.error("Error dispatch: method={} uri={} status={} message={} errorType={} originalUri={} servlet={} exception={}",
              request.getMethod(), request.getRequestURI(), status, message, errorType, requestUri, servletName, String.valueOf(error), error);
        } else {
          log.error("Error dispatch: method={} uri={} status={} message={} errorType={} originalUri={} servlet={} exception={}",
              request.getMethod(), request.getRequestURI(), status, message, errorType, requestUri, servletName, String.valueOf(error));
        }
      } else {
        // In some flows container calls sendError(...) without an exception object.
        if (response.getStatus() >= 500) {
          log.error("Server error without exception object: {} {} -> status={}", request.getMethod(), request.getRequestURI(), response.getStatus());
        }
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


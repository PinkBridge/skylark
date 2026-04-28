package cn.skylark.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class SkylarkGlobalExceptionHandler {

  private final SkylarkWebExceptionProperties props;

  public SkylarkGlobalExceptionHandler(SkylarkWebExceptionProperties props) {
    this.props = props;
  }

  @ExceptionHandler({
      IllegalArgumentException.class,
      MissingServletRequestParameterException.class,
      BindException.class,
      MultipartException.class,
      HttpMessageNotReadableException.class,
      IOException.class
  })
  public ResponseEntity<Object> handleBadRequest(Exception e, HttpServletRequest request) {
    if (props.isLogBadRequestStacktrace()) {
      log.warn("Bad request: {} {} -> {}", request.getMethod(), request.getRequestURI(), e.toString(), e);
    } else {
      log.warn("Bad request: {} {} -> {}", request.getMethod(), request.getRequestURI(), e.toString());
    }
    Object body = SkylarkResponseBodies.fail(HttpStatus.BAD_REQUEST.value(), safeMessage(e));
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleAny(Exception e, HttpServletRequest request) {
    log.error("Unhandled error: {} {} -> {}", request.getMethod(), request.getRequestURI(), e.toString(), e);
    Object body = SkylarkResponseBodies.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal.server.error");
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(body);
  }

  private static String safeMessage(Exception e) {
    String msg = e.getMessage();
    if (msg == null || msg.trim().isEmpty()) {
      return "bad.request";
    }
    return msg.trim();
  }
}


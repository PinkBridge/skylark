package cn.skylark.aiot_service.iot.mgmt.controller;

import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MgmtExceptionHandler {
  @ExceptionHandler(MgmtException.class)
  public ResponseEntity<Map<String, Object>> handleMgmtException(MgmtException ex) {
    Map<String, Object> body = new HashMap<String, Object>();
    body.put("message", ex.getMessage());
    body.put("status", ex.getStatus().value());
    return ResponseEntity.status(ex.getStatus()).body(body);
  }
}


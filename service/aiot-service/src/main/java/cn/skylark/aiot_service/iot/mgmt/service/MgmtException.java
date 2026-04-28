package cn.skylark.aiot_service.iot.mgmt.service;

import org.springframework.http.HttpStatus;

public class MgmtException extends RuntimeException {
  private final HttpStatus status;

  public MgmtException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}


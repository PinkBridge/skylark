package cn.skylark.aiot_service.iot.notification.dto;

import lombok.Data;

@Data
public class NotificationTestResult {
  private boolean ok;
  private String error;

  public static NotificationTestResult ok() {
    NotificationTestResult r = new NotificationTestResult();
    r.ok = true;
    return r;
  }

  public static NotificationTestResult fail(String error) {
    NotificationTestResult r = new NotificationTestResult();
    r.ok = false;
    r.error = error;
    return r;
  }
}


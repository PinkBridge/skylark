package cn.skylark.aiot_service.iot.access.listener;

import cn.skylark.aiot_service.iot.access.service.ThingServiceReplyAwaiter;
import cn.skylark.aiot_service.iot.appint.model.IotIntegrationEventType;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class ThingServiceReplyListener {
  private final ThingServiceReplyAwaiter awaiter;

  public ThingServiceReplyListener(ThingServiceReplyAwaiter awaiter) {
    this.awaiter = awaiter;
  }

  @EventListener
  public void on(NormalizedEvent event) {
    if (event == null || !IotIntegrationEventType.SERVICE_REPLY.equals(event.getEventType())) {
      return;
    }
    Map<String, String> subject = event.getSubject();
    Map<String, Object> data = event.getData();
    if (subject == null || data == null) {
      return;
    }
    String productKey = trimToNull(subject.get("productKey"));
    String deviceKey = trimToNull(subject.get("deviceKey"));
    String serviceName = trimToNull(asString(data.get("serviceName")));
    String messageId = trimToNull(asString(data.get("messageId")));
    if (!StringUtils.hasText(productKey)
        || !StringUtils.hasText(deviceKey)
        || !StringUtils.hasText(serviceName)
        || !StringUtils.hasText(messageId)) {
      return;
    }
    String key = buildKey(productKey, deviceKey, serviceName, messageId);
    awaiter.complete(key, asString(data.get("payload")));
  }

  public static String buildKey(String productKey, String deviceName, String identifier, String messageId) {
    return safe(productKey) + "|" + safe(deviceName) + "|" + safe(identifier) + "|" + safe(messageId);
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private static String trimToNull(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String text = value.trim();
    return text.isEmpty() ? null : text;
  }

  private static String safe(String value) {
    return value == null ? "" : value.trim();
  }
}

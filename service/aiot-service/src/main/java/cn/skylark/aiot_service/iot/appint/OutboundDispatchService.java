package cn.skylark.aiot_service.iot.appint;

import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.appint.model.OutboundDispatchRow;
import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundDeliveryMapper;
import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundSubscriptionMapper;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceGroupRelMapper;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundDeliveryEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OutboundDispatchService {
  private static final Logger log = LoggerFactory.getLogger(OutboundDispatchService.class);
  private static final int MAX_SNAPSHOT_CHARS = 32_000;

  private final IotOutboundSubscriptionMapper subscriptionMapper;
  private final IotOutboundDeliveryMapper deliveryMapper;
  private final WebhookOutboundClient webhookOutboundClient;
  private final MqttOutboundClient mqttOutboundClient;
  private final ObjectMapper objectMapper;
  private final DeviceGroupRelMapper deviceGroupRelMapper;

  public OutboundDispatchService(IotOutboundSubscriptionMapper subscriptionMapper,
                                 IotOutboundDeliveryMapper deliveryMapper,
                                 WebhookOutboundClient webhookOutboundClient,
                                  MqttOutboundClient mqttOutboundClient,
                                  ObjectMapper objectMapper,
                                  DeviceGroupRelMapper deviceGroupRelMapper) {
    this.subscriptionMapper = subscriptionMapper;
    this.deliveryMapper = deliveryMapper;
    this.webhookOutboundClient = webhookOutboundClient;
    this.mqttOutboundClient = mqttOutboundClient;
    this.objectMapper = objectMapper;
    this.deviceGroupRelMapper = deviceGroupRelMapper;
  }

  public void dispatch(NormalizedEvent event) {
    if (event == null || event.getTenantId() == null) {
      return;
    }
    List<OutboundDispatchRow> rows = subscriptionMapper.listDispatchRows(event.getTenantId());
    if (rows == null || rows.isEmpty()) {
      return;
    }
    String body = envelopeJson(event);
    for (OutboundDispatchRow row : rows) {
      if (!OutboundSubscriptionMatcher.matchOrg(row, event)) {
        continue;
      }
      if (!OutboundSubscriptionMatcher.matchEventType(row.getEventTypes(), event.getEventType(), objectMapper)) {
        continue;
      }
      if (!OutboundSubscriptionMatcher.matchFilter(row.getFilterJson(), event, objectMapper, deviceGroupRelMapper)) {
        continue;
      }
      String ct = safe(row.getChannelType()).toUpperCase();
      if ("WEBHOOK".equals(ct)) {
        WebhookChannelConfig cfg = WebhookChannelConfig.parse(row.getChannelConfigJson(), objectMapper);
        if (!cfg.isValid()) {
          log.warn("integration webhook skip: invalid channel config channelId={}", row.getChannelId());
          continue;
        }
        sendWebhookWithDelivery(event, row, body, cfg);
      } else if ("MQTT".equals(ct)) {
        MqttChannelConfig cfg = MqttChannelConfig.parse(row.getChannelConfigJson(), objectMapper);
        if (!cfg.isValid()) {
          log.warn("integration mqtt skip: invalid channel config channelId={}", row.getChannelId());
          continue;
        }
        sendMqttWithDelivery(event, row, body, cfg);
      }
    }
  }

  /**
   * Sends a one-off test delivery for a channel (same Webhook path as live events).
   */
  public WebhookOutboundClient.WebhookSendResult dispatchTestWebhook(Long channelId, String channelType, String configJson,
                                                                   NormalizedEvent sampleEvent) {
    if (!"WEBHOOK".equalsIgnoreCase(safe(channelType))) {
      return WebhookOutboundClient.WebhookSendResult.failure(0, "only WEBHOOK supported in this version");
    }
    WebhookChannelConfig cfg = WebhookChannelConfig.parse(configJson, objectMapper);
    if (!cfg.isValid()) {
      return WebhookOutboundClient.WebhookSendResult.failure(0, "invalid webhook config (need url)");
    }
    String body = envelopeJson(sampleEvent);
    return webhookOutboundClient.postJson(cfg.url, body, cfg.signingSecret, cfg.readTimeoutMs);
  }

  public MqttOutboundClient.PublishResult dispatchTestMqtt(String channelType, String configJson, NormalizedEvent sampleEvent) {
    if (!"MQTT".equalsIgnoreCase(safe(channelType))) {
      return MqttOutboundClient.PublishResult.failure("only MQTT supported in this test");
    }
    MqttChannelConfig cfg = MqttChannelConfig.parse(configJson, objectMapper);
    if (!cfg.isValid()) {
      return MqttOutboundClient.PublishResult.failure("invalid mqtt config (need brokerUrl/topic)");
    }
    String body = envelopeJson(sampleEvent);
    return mqttOutboundClient.publishJson(cfg.brokerUrl, cfg.clientId, cfg.username, cfg.password, cfg.topic, cfg.qos, body);
  }

  static String envelopeJson(NormalizedEvent event, ObjectMapper objectMapper) {
    Map<String, Object> envelope = new LinkedHashMap<String, Object>();
    envelope.put("eventId", event.getEventId());
    envelope.put("eventType", event.getEventType());
    envelope.put("occurredAt", event.getOccurredAt());
    envelope.put("tenantId", event.getTenantId());
    envelope.put("orgId", event.getOrgId());
    envelope.put("source", event.getSource());
    envelope.put("subject", event.getSubject());
    envelope.put("data", event.getData());
    try {
      return objectMapper.writeValueAsString(envelope);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("cannot serialize event", e);
    }
  }

  private String envelopeJson(NormalizedEvent event) {
    return envelopeJson(event, objectMapper);
  }

  private void sendWebhookWithDelivery(NormalizedEvent event, OutboundDispatchRow row, String body, WebhookChannelConfig cfg) {
    String snapshot = truncate(body, MAX_SNAPSHOT_CHARS);
    IotOutboundDeliveryEntity d = new IotOutboundDeliveryEntity();
    d.setTenantId(event.getTenantId());
    d.setOrgId(event.getOrgId());
    d.setEventId(event.getEventId());
    d.setEventType(event.getEventType());
    d.setSubscriptionId(row.getSubscriptionId());
    d.setChannelId(row.getChannelId());
    d.setStatus("pending");
    d.setAttempts(0);
    d.setPayloadSnapshot(snapshot);
    deliveryMapper.insert(d);

    WebhookOutboundClient.WebhookSendResult result = webhookOutboundClient.postJson(cfg.url, body, cfg.signingSecret, cfg.readTimeoutMs);
    d.setAttempts(1);
    if (result.isOk()) {
      d.setStatus("success");
      d.setHttpStatus(result.getHttpStatus());
      d.setLastError(null);
      d.setNextRetryAt(null);
    } else {
      d.setStatus("failed");
      d.setHttpStatus(result.getHttpStatus() > 0 ? result.getHttpStatus() : null);
      d.setLastError(result.getError());
      d.setNextRetryAt(LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(retryDelaySeconds(1)));
    }
    deliveryMapper.update(d);
  }

  private void sendMqttWithDelivery(NormalizedEvent event, OutboundDispatchRow row, String body, MqttChannelConfig cfg) {
    String snapshot = truncate(body, MAX_SNAPSHOT_CHARS);
    IotOutboundDeliveryEntity d = new IotOutboundDeliveryEntity();
    d.setTenantId(event.getTenantId());
    d.setOrgId(event.getOrgId());
    d.setEventId(event.getEventId());
    d.setEventType(event.getEventType());
    d.setSubscriptionId(row.getSubscriptionId());
    d.setChannelId(row.getChannelId());
    d.setStatus("pending");
    d.setAttempts(0);
    d.setPayloadSnapshot(snapshot);
    deliveryMapper.insert(d);

    MqttOutboundClient.PublishResult result =
        mqttOutboundClient.publishJson(cfg.brokerUrl, cfg.clientId, cfg.username, cfg.password, cfg.topic, cfg.qos, body);
    d.setAttempts(1);
    if (result.isOk()) {
      d.setStatus("success");
      d.setHttpStatus(null);
      d.setLastError(null);
      d.setNextRetryAt(null);
    } else {
      d.setStatus("failed");
      d.setHttpStatus(null);
      d.setLastError(result.getError());
      d.setNextRetryAt(LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(retryDelaySeconds(1)));
    }
    deliveryMapper.update(d);
  }

  static long retryDelaySeconds(int attemptsAfterFailure) {
    long sec = 1L << Math.min(attemptsAfterFailure, 16);
    return Math.min(sec, 900L);
  }

  static LocalDateTime nextRetryTime(int attemptsAfterFailure) {
    return LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(retryDelaySeconds(attemptsAfterFailure));
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return null;
    }
    if (s.length() <= max) {
      return s;
    }
    return s.substring(0, max);
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  public static final class WebhookChannelConfig {
    final String url;
    final String signingSecret;
    final int readTimeoutMs;

    WebhookChannelConfig(String url, String signingSecret, int readTimeoutMs) {
      this.url = url;
      this.signingSecret = signingSecret;
      this.readTimeoutMs = readTimeoutMs;
    }

    boolean isValid() {
      return StringUtils.hasText(url);
    }

    static WebhookChannelConfig parse(String configJson, ObjectMapper mapper) {
      if (!StringUtils.hasText(configJson)) {
        return new WebhookChannelConfig("", "", 30_000);
      }
      try {
        JsonNode n = mapper.readTree(configJson);
        String url = text(n, "url");
        String secret = firstText(n, "signingSecret", "signing_secret", "secret");
        int sec = n.path("timeoutSeconds").asInt(30);
        int readMs = sec <= 0 ? 30_000 : Math.min(sec * 1000, 120_000);
        return new WebhookChannelConfig(url, secret, readMs);
      } catch (Exception e) {
        return new WebhookChannelConfig("", "", 30_000);
      }
    }

    private static String text(JsonNode n, String field) {
      JsonNode v = n.get(field);
      if (v != null && v.isTextual()) {
        String t = v.asText();
        return StringUtils.hasText(t) ? t.trim() : "";
      }
      return "";
    }

    private static String firstText(JsonNode n, String... fields) {
      for (String f : fields) {
        String t = text(n, f);
        if (StringUtils.hasText(t)) {
          return t;
        }
      }
      return "";
    }
  }

  public static final class MqttChannelConfig {
    final String brokerUrl;
    final String topic;
    final String clientId;
    final String username;
    final String password;
    final int qos;

    MqttChannelConfig(String brokerUrl, String topic, String clientId, String username, String password, int qos) {
      this.brokerUrl = brokerUrl;
      this.topic = topic;
      this.clientId = clientId;
      this.username = username;
      this.password = password;
      this.qos = qos;
    }

    boolean isValid() {
      return StringUtils.hasText(brokerUrl) && StringUtils.hasText(topic);
    }

    static MqttChannelConfig parse(String configJson, ObjectMapper mapper) {
      if (!StringUtils.hasText(configJson)) {
        return new MqttChannelConfig("", "", "", "", "", 0);
      }
      try {
        JsonNode n = mapper.readTree(configJson);
        String broker = firstText(n, "brokerUrl", "broker", "url", "server");
        String topic = text(n, "topic");
        String clientId = firstText(n, "clientId", "client_id");
        String username = firstText(n, "username", "user");
        String password = firstText(n, "password", "pass");
        int qos = n.path("qos").asInt(0);
        int q = Math.max(0, Math.min(qos, 2));
        return new MqttChannelConfig(broker, topic, clientId, username, password, q);
      } catch (Exception e) {
        return new MqttChannelConfig("", "", "", "", "", 0);
      }
    }

    private static String text(JsonNode n, String field) {
      JsonNode v = n.get(field);
      if (v != null && v.isTextual()) {
        String t = v.asText();
        return StringUtils.hasText(t) ? t.trim() : "";
      }
      return "";
    }

    private static String firstText(JsonNode n, String... fields) {
      for (String f : fields) {
        String t = text(n, f);
        if (StringUtils.hasText(t)) {
          return t;
        }
      }
      return "";
    }
  }
}

package cn.skylark.aiot_service.iot.notification.service;

import cn.skylark.aiot_service.iot.appint.OutboundDispatchService;
import cn.skylark.aiot_service.iot.appint.OutboundSubscriptionMatcher;
import cn.skylark.aiot_service.iot.appint.WebhookOutboundClient;
import cn.skylark.aiot_service.iot.appint.model.IotIntegrationEventType;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.notification.dto.NotificationTestResult;
import cn.skylark.aiot_service.iot.notification.entity.IotNotificationChannelEntity;
import cn.skylark.aiot_service.iot.notification.entity.IotNotificationDeliveryEntity;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationChannelMapper;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationDeliveryMapper;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationSubscriptionMapper;
import cn.skylark.aiot_service.iot.notification.model.NotificationDispatchRow;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceGroupRelMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
public class NotificationDispatchService {
  private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);
  private static final int MAX_SNAPSHOT_CHARS = 32_000;

  private final IotNotificationSubscriptionMapper subscriptionMapper;
  private final IotNotificationDeliveryMapper deliveryMapper;
  private final IotNotificationChannelMapper channelMapper;
  private final DeviceGroupRelMapper deviceGroupRelMapper;
  private final WebhookOutboundClient webhookOutboundClient;
  private final NotificationTemplateRenderer templateRenderer;
  private final ObjectMapper objectMapper;

  @Value("${iot.notification.retry.max-attempts:8}")
  private int maxRetryAttempts;

  public NotificationDispatchService(IotNotificationSubscriptionMapper subscriptionMapper,
                                     IotNotificationDeliveryMapper deliveryMapper,
                                     IotNotificationChannelMapper channelMapper,
                                     DeviceGroupRelMapper deviceGroupRelMapper,
                                     WebhookOutboundClient webhookOutboundClient,
                                     NotificationTemplateRenderer templateRenderer,
                                     ObjectMapper objectMapper) {
    this.subscriptionMapper = subscriptionMapper;
    this.deliveryMapper = deliveryMapper;
    this.channelMapper = channelMapper;
    this.deviceGroupRelMapper = deviceGroupRelMapper;
    this.webhookOutboundClient = webhookOutboundClient;
    this.templateRenderer = templateRenderer;
    this.objectMapper = objectMapper;
  }

  public void dispatch(NormalizedEvent event) {
    if (event == null || event.getTenantId() == null) {
      return;
    }
    List<String> groupKeys = resolveGroupKeys(event);
    if (groupKeys.isEmpty()) {
      return;
    }
    List<NotificationDispatchRow> rows = subscriptionMapper.listDispatchRowsByGroupKeys(event.getTenantId(), groupKeys);
    if (rows == null || rows.isEmpty()) {
      return;
    }
    for (NotificationDispatchRow row : rows) {
      try {
        dispatchOne(event, row);
      } catch (RuntimeException e) {
        log.warn("notification dispatch failed eventId={} subId={}", event.getEventId(), row.getSubscriptionId(), e);
      }
    }
  }

  private void dispatchOne(NormalizedEvent event, NotificationDispatchRow row) {
    if (row.getSubscriptionOrgId() != null
        && (event.getOrgId() == null || !row.getSubscriptionOrgId().equals(event.getOrgId()))) {
      return;
    }
    if (!OutboundSubscriptionMatcher.matchEventType(row.getEventTypes(), event.getEventType(), objectMapper)) {
      return;
    }
    if (!OutboundSubscriptionMatcher.matchFilter(row.getFilterJson(), event, objectMapper, deviceGroupRelMapper)) {
      return;
    }
    TemplateParts parts = parseTemplateJson(row.getTemplateJson());
    String title = templateRenderer.render(parts.titleTemplate, event);
    String body = templateRenderer.render(parts.bodyTemplate, event);
    ChannelSendResult send = sendToChannel(row.getChannelType(), row.getChannelConfigJson(), title, body);
    persistDelivery(event, row, title, body, send, 1);
  }

  public NotificationTestResult testChannel(Long tenantId, IotNotificationChannelEntity ch) {
    if (ch == null) {
      return NotificationTestResult.fail("channel not found");
    }
    NormalizedEvent sample = sampleEvent(tenantId, ch.getOrgId());
    String title = "[Skylark] Notification test";
    String body = "Channel connectivity test.\neventType=" + sample.getEventType() + "\nproductKey="
        + safeSubject(sample, "productKey") + "\ndeviceKey=" + safeSubject(sample, "deviceKey");
    ChannelSendResult r = sendToChannel(ch.getType(), ch.getConfigJson(), title, body);
    if (r.isOk()) {
      return NotificationTestResult.ok();
    }
    return NotificationTestResult.fail(r.getError() == null ? "send failed" : r.getError());
  }

  /**
   * Retry scheduler: resend using stored payload when possible, otherwise rebuild from channel type + rendered text.
   */
  public void resendDelivery(IotNotificationDeliveryEntity d) {
    if (d == null || d.getTenantId() == null) {
      return;
    }
    IotNotificationChannelEntity ch = channelMapper.findById(d.getTenantId(), d.getChannelId());
    if (ch == null || ch.getEnabled() == null || ch.getEnabled() == 0) {
      return;
    }
    String title = d.getRenderedTitle() == null ? "" : d.getRenderedTitle();
    String body = d.getRenderedBody() == null ? "" : d.getRenderedBody();
    ChannelSendResult send;
    if (StringUtils.hasText(d.getPayloadSnapshot()) && isHttpChannel(ch.getType())) {
      send = resendHttpPayload(ch.getType(), ch.getConfigJson(), d.getPayloadSnapshot());
    } else {
      send = sendToChannel(ch.getType(), ch.getConfigJson(), title, body);
    }
    int prev = d.getAttempts() == null ? 0 : d.getAttempts();
    int nextAttempt = prev + 1;
    d.setAttempts(nextAttempt);
    if (send.isOk()) {
      d.setStatus("success");
      d.setHttpStatus(send.getHttpStatus());
      d.setLastError(null);
      d.setNextRetryAt(null);
      if (StringUtils.hasText(send.getPayloadSnapshot())) {
        d.setPayloadSnapshot(truncate(send.getPayloadSnapshot(), MAX_SNAPSHOT_CHARS));
      }
    } else {
      d.setHttpStatus(send.getHttpStatus());
      d.setLastError(truncateError(send.getError()));
      if (nextAttempt >= maxRetryAttempts) {
        d.setStatus("dead");
        d.setNextRetryAt(null);
      } else {
        d.setStatus("failed");
        d.setNextRetryAt(nextRetryTime(nextAttempt));
      }
    }
    deliveryMapper.update(d);
  }

  private static boolean isHttpChannel(String type) {
    if (type == null) {
      return false;
    }
    String t = type.trim().toUpperCase();
    return "DINGTALK".equals(t) || "WECOM".equals(t) || "FEISHU".equals(t);
  }

  private ChannelSendResult resendHttpPayload(String channelType, String configJson, String payloadSnapshot) {
    String url = extractWebhookUrl(configJson);
    if (!StringUtils.hasText(url)) {
      return ChannelSendResult.failure(null, "webhook url missing");
    }
    int timeoutMs = readTimeoutMs(configJson);
    WebhookOutboundClient.WebhookSendResult r =
        webhookOutboundClient.postJson(url, payloadSnapshot, "", timeoutMs);
    if (r.isOk()) {
      return ChannelSendResult.success(r.getHttpStatus(), payloadSnapshot);
    }
    return ChannelSendResult.failure(r.getHttpStatus() > 0 ? r.getHttpStatus() : null, r.getError());
  }

  private void persistDelivery(NormalizedEvent event,
                               NotificationDispatchRow row,
                               String renderedTitle,
                               String renderedBody,
                               ChannelSendResult send,
                               int attemptsAfterSend) {
    IotNotificationDeliveryEntity d = new IotNotificationDeliveryEntity();
    d.setTenantId(event.getTenantId());
    d.setOrgId(event.getOrgId());
    d.setEventId(event.getEventId());
    d.setEventType(event.getEventType());
    d.setSubscriptionId(row.getSubscriptionId());
    d.setChannelId(row.getChannelId());
    d.setRenderedTitle(truncate(renderedTitle, 256));
    d.setRenderedBody(renderedBody);
    d.setStatus("pending");
    d.setAttempts(0);
    d.setPayloadSnapshot(truncate(send.getPayloadSnapshot(), MAX_SNAPSHOT_CHARS));
    deliveryMapper.insert(d);

    d.setAttempts(attemptsAfterSend);
    if (send.isOk()) {
      d.setStatus("success");
      d.setHttpStatus(send.getHttpStatus());
      d.setLastError(null);
      d.setNextRetryAt(null);
    } else {
      d.setStatus("failed");
      d.setHttpStatus(send.getHttpStatus());
      d.setLastError(truncateError(send.getError()));
      d.setNextRetryAt(nextRetryTime(attemptsAfterSend));
    }
    deliveryMapper.update(d);
  }

  private static LocalDateTime nextRetryTime(int attemptsAfterFailure) {
    return LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(retryDelaySeconds(attemptsAfterFailure));
  }

  private static long retryDelaySeconds(int attemptsAfterFailure) {
    long sec = 1L << Math.min(attemptsAfterFailure, 16);
    return Math.min(sec, 900L);
  }

  private List<String> resolveGroupKeys(NormalizedEvent event) {
    LinkedHashSet<String> keys = new LinkedHashSet<String>();
    String pk = subject(event, "productKey");
    String dk = subject(event, "deviceKey");
    if (event.getTenantId() != null && StringUtils.hasText(pk) && StringUtils.hasText(dk)) {
      List<String> fromDb = this.deviceGroupRelMapper.listGroupKeysByDevice(event.getTenantId(), pk, dk);
      if (fromDb != null) {
        for (String g : fromDb) {
          if (StringUtils.hasText(g)) {
            keys.add(g.trim());
          }
        }
      }
    }
    String gk = subject(event, "deviceGroupKey");
    if (StringUtils.hasText(gk)) {
      keys.add(gk.trim());
    }
    return new ArrayList<String>(keys);
  }

  private static String subject(NormalizedEvent event, String key) {
    if (event.getSubject() == null) {
      return null;
    }
    return event.getSubject().get(key);
  }

  private static String safeSubject(NormalizedEvent event, String key) {
    String s = subject(event, key);
    return s == null ? "" : s;
  }

  private ChannelSendResult sendToChannel(String channelType, String configJson, String title, String body) {
    if (!StringUtils.hasText(channelType)) {
      return ChannelSendResult.failure(null, "channel type empty");
    }
    String t = channelType.trim().toUpperCase();
    try {
      if ("DINGTALK".equals(t)) {
        return sendDingTalk(configJson, title, body);
      }
      if ("WECOM".equals(t)) {
        return sendWecom(configJson, title, body);
      }
      if ("FEISHU".equals(t)) {
        return sendFeishu(configJson, title, body);
      }
      if ("EMAIL".equals(t)) {
        return sendEmail(configJson, title, body);
      }
      if ("SMS".equals(t)) {
        return sendSmsStub(configJson, title, body);
      }
      return ChannelSendResult.failure(null, "unsupported channel type: " + t);
    } catch (Exception e) {
      String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
      return ChannelSendResult.failure(null, truncateError(msg));
    }
  }

  private ChannelSendResult sendDingTalk(String configJson, String title, String body) throws Exception {
    String url = extractWebhookUrl(configJson);
    if (!StringUtils.hasText(url)) {
      return ChannelSendResult.failure(null, "configJson.webhook required");
    }
    ObjectNode root = objectMapper.createObjectNode();
    String text = mergeTitleBody(title, body);
    if (StringUtils.hasText(title) && StringUtils.hasText(body)) {
      root.put("msgtype", "markdown");
      ObjectNode md = objectMapper.createObjectNode();
      md.put("title", title);
      md.put("text", body);
      root.set("markdown", md);
    } else {
      root.put("msgtype", "text");
      ObjectNode textNode = objectMapper.createObjectNode();
      textNode.put("content", text);
      root.set("text", textNode);
    }
    String json = objectMapper.writeValueAsString(root);
    int timeoutMs = readTimeoutMs(configJson);
    WebhookOutboundClient.WebhookSendResult r = webhookOutboundClient.postJson(url, json, "", timeoutMs);
    if (r.isOk()) {
      return ChannelSendResult.success(r.getHttpStatus(), json);
    }
    return ChannelSendResult.failure(r.getHttpStatus() > 0 ? r.getHttpStatus() : null, r.getError());
  }

  private ChannelSendResult sendWecom(String configJson, String title, String body) throws Exception {
    String url = extractWebhookUrl(configJson);
    if (!StringUtils.hasText(url)) {
      return ChannelSendResult.failure(null, "configJson.webhook required");
    }
    ObjectNode root = objectMapper.createObjectNode();
    String content = StringUtils.hasText(title) ? ("## " + title + "\n\n" + body) : body;
    root.put("msgtype", "markdown");
    ObjectNode md = objectMapper.createObjectNode();
    md.put("content", content);
    root.set("markdown", md);
    String json = objectMapper.writeValueAsString(root);
    int timeoutMs = readTimeoutMs(configJson);
    WebhookOutboundClient.WebhookSendResult r = webhookOutboundClient.postJson(url, json, "", timeoutMs);
    if (r.isOk()) {
      return ChannelSendResult.success(r.getHttpStatus(), json);
    }
    return ChannelSendResult.failure(r.getHttpStatus() > 0 ? r.getHttpStatus() : null, r.getError());
  }

  private ChannelSendResult sendFeishu(String configJson, String title, String body) throws Exception {
    String url = extractWebhookUrl(configJson);
    if (!StringUtils.hasText(url)) {
      return ChannelSendResult.failure(null, "configJson.webhook required");
    }
    ObjectNode root = objectMapper.createObjectNode();
    root.put("msg_type", "text");
    ObjectNode content = objectMapper.createObjectNode();
    content.put("text", mergeTitleBody(title, body));
    root.set("content", content);
    String json = objectMapper.writeValueAsString(root);
    int timeoutMs = readTimeoutMs(configJson);
    WebhookOutboundClient.WebhookSendResult r = webhookOutboundClient.postJson(url, json, "", timeoutMs);
    if (r.isOk()) {
      return ChannelSendResult.success(r.getHttpStatus(), json);
    }
    return ChannelSendResult.failure(r.getHttpStatus() > 0 ? r.getHttpStatus() : null, r.getError());
  }

  private ChannelSendResult sendEmail(String configJson, String title, String body) throws Exception {
    JsonNode n = readConfig(configJson);
    String host = firstText(n, "host", "smtpHost");
    String from = firstText(n, "from", "mailFrom");
    String to = firstText(n, "to", "mailTo", "recipients");
    if (!StringUtils.hasText(host) || !StringUtils.hasText(from) || !StringUtils.hasText(to)) {
      return ChannelSendResult.failure(null, "email config requires host, from, to");
    }
    int port = n.path("port").asInt(587);
    String username = firstText(n, "username", "user");
    String password = firstText(n, "password", "pass");
    boolean startTls = !n.has("startTls") || n.path("startTls").asBoolean(true);

    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host.trim());
    sender.setPort(port);
    sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
    if (StringUtils.hasText(username)) {
      sender.setUsername(username.trim());
    }
    if (StringUtils.hasText(password)) {
      sender.setPassword(password);
    }
    Properties props = sender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", String.valueOf(StringUtils.hasText(username)));
    props.put("mail.smtp.starttls.enable", String.valueOf(startTls));
    props.put("mail.debug", "false");

    MimeMessage message = sender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
    helper.setFrom(from.trim());
    helper.setTo(splitAddresses(to));
    helper.setSubject(title == null ? "" : title);
    helper.setText(body == null ? "" : body, false);

    sender.send(message);

    ObjectNode snap = objectMapper.createObjectNode();
    snap.put("kind", "EMAIL");
    snap.put("subject", title);
    snap.put("to", to);
    return ChannelSendResult.success(null, objectMapper.writeValueAsString(snap));
  }

  private ChannelSendResult sendSmsStub(String configJson, String title, String body) throws Exception {
    ObjectNode snap = objectMapper.createObjectNode();
    snap.put("kind", "SMS");
    snap.put("stub", true);
    snap.put("title", title);
    snap.put("body", body);
    log.info("SMS notification stub (no provider wired) title={}", title);
    return ChannelSendResult.success(null, objectMapper.writeValueAsString(snap));
  }

  private static String[] splitAddresses(String to) {
    String[] parts = to.split("[,;\\s]+");
    List<String> out = new ArrayList<String>();
    for (String p : parts) {
      if (StringUtils.hasText(p)) {
        out.add(p.trim());
      }
    }
    return out.toArray(new String[0]);
  }

  private static String mergeTitleBody(String title, String body) {
    if (StringUtils.hasText(title) && StringUtils.hasText(body)) {
      return title + "\n\n" + body;
    }
    if (StringUtils.hasText(title)) {
      return title;
    }
    return body == null ? "" : body;
  }

  private JsonNode readConfig(String configJson) throws Exception {
    if (!StringUtils.hasText(configJson)) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(configJson);
  }

  private String extractWebhookUrl(String configJson) {
    if (!StringUtils.hasText(configJson)) {
      return "";
    }
    try {
      JsonNode n = objectMapper.readTree(configJson);
      return firstText(n, "webhook", "url", "endpoint");
    } catch (Exception e) {
      return "";
    }
  }

  private int readTimeoutMs(String configJson) {
    try {
      JsonNode n = readConfig(configJson);
      int sec = n.path("timeoutSeconds").asInt(30);
      if (sec <= 0) {
        return 30_000;
      }
      return Math.min(sec * 1000, 120_000);
    } catch (Exception e) {
      return 30_000;
    }
  }

  private static String firstText(JsonNode n, String... fields) {
    for (String f : fields) {
      JsonNode v = n.get(f);
      if (v != null && v.isTextual()) {
        String t = v.asText();
        if (StringUtils.hasText(t)) {
          return t.trim();
        }
      }
    }
    return "";
  }

  private TemplateParts parseTemplateJson(String templateJson) {
    TemplateParts p = new TemplateParts();
    p.titleTemplate = "";
    p.bodyTemplate = "";
    if (!StringUtils.hasText(templateJson)) {
      return p;
    }
    try {
      JsonNode n = objectMapper.readTree(templateJson);
      if (n != null && n.has("title")) {
        p.titleTemplate = n.get("title").asText("");
      }
      if (n != null && n.has("body")) {
        p.bodyTemplate = n.get("body").asText("");
      }
    } catch (Exception e) {
      log.warn("invalid template json on subscription");
    }
    return p;
  }

  private static NormalizedEvent sampleEvent(Long tenantId, Long orgId) {
    Map<String, String> subject = new java.util.LinkedHashMap<String, String>();
    subject.put("productKey", "demoProduct");
    subject.put("deviceKey", "demoDevice");
    subject.put("deviceGroupKey", "demoGroup");
    Map<String, Object> data = new java.util.LinkedHashMap<String, Object>();
    data.put("message", "notification channel test");
    return NormalizedEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(IotIntegrationEventType.PROPERTY_REPORTED)
        .occurredAt(System.currentTimeMillis())
        .tenantId(tenantId)
        .orgId(orgId)
        .source("aiot-service")
        .subject(subject)
        .data(data)
        .build();
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

  private static String truncateError(String err) {
    if (err == null) {
      return null;
    }
    if (err.length() <= 1000) {
      return err;
    }
    return err.substring(0, 1000);
  }

  private static final class TemplateParts {
    private String titleTemplate;
    private String bodyTemplate;
  }

  private static final class ChannelSendResult {
    private final boolean ok;
    private final Integer httpStatus;
    private final String error;
    private final String payloadSnapshot;

    private ChannelSendResult(boolean ok, Integer httpStatus, String error, String payloadSnapshot) {
      this.ok = ok;
      this.httpStatus = httpStatus;
      this.error = error;
      this.payloadSnapshot = payloadSnapshot;
    }

    static ChannelSendResult success(Integer httpStatus, String payloadSnapshot) {
      return new ChannelSendResult(true, httpStatus, null, payloadSnapshot);
    }

    static ChannelSendResult failure(Integer httpStatus, String error) {
      return new ChannelSendResult(false, httpStatus, error, null);
    }

    boolean isOk() {
      return ok;
    }

    Integer getHttpStatus() {
      return httpStatus;
    }

    String getError() {
      return error;
    }

    String getPayloadSnapshot() {
      return payloadSnapshot;
    }
  }
}

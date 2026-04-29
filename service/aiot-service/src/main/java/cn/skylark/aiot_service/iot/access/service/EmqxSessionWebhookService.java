package cn.skylark.aiot_service.iot.access.service;

import cn.skylark.aiot_service.iot.access.mapper.AccessDeviceMapper;
import cn.skylark.aiot_service.iot.access.model.AccessDeviceRecord;
import cn.skylark.aiot_service.iot.access.model.EmqxClientSessionEvent;
import cn.skylark.aiot_service.iot.appint.NormalizedEventPublisher;
import cn.skylark.aiot_service.iot.appint.model.IotIntegrationEventType;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.mgmt.model.dto.CreateDeviceConnectRecordRequest;
import cn.skylark.aiot_service.iot.mgmt.service.DeviceService;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class EmqxSessionWebhookService {
  private static final Logger log = LoggerFactory.getLogger(EmqxSessionWebhookService.class);

  private static final String CONNECTED = "connected";
  private static final String DISCONNECTED = "disconnected";

  private final AccessDeviceMapper accessDeviceMapper;
  private final DeviceService deviceService;
  private final NormalizedEventPublisher normalizedEventPublisher;

  public EmqxSessionWebhookService(AccessDeviceMapper accessDeviceMapper,
                                   DeviceService deviceService,
                                   NormalizedEventPublisher normalizedEventPublisher) {
    this.accessDeviceMapper = accessDeviceMapper;
    this.deviceService = deviceService;
    this.normalizedEventPublisher = normalizedEventPublisher;
  }

  public boolean handleSessionEvent(EmqxClientSessionEvent body) {
    if (body == null) {
      return false;
    }
    String action = resolveAction(body);
    String deviceKey = safe(body.getUsername());
    if (!StringUtils.hasText(deviceKey)) {
      deviceKey = safe(body.getClientid());
      if (StringUtils.hasText(deviceKey)) {
        log.info("emqx webhook fallback: username empty, use clientid as device_key={}", deviceKey);
      }
    }
    if (!StringUtils.hasText(deviceKey)) {
      log.warn("emqx webhook skipped: empty username/clientid (device_key)");
      return false;
    }

    List<AccessDeviceRecord> rows = accessDeviceMapper.findByDeviceKey(deviceKey);
    if (rows == null || rows.isEmpty()) {
      log.info("emqx webhook: no device for username/device_key={}", deviceKey);
      return false;
    }

    AccessDeviceRecord dev = rows.get(0);
    CreateDeviceConnectRecordRequest req = new CreateDeviceConnectRecordRequest();
    req.setAction(action);
    req.setClientId(trimToNull(body.getClientid(), 128));
    req.setIp(trimToNull(parseIpFromPeername(body.getPeername()), 64));
    req.setUserAgent(buildUserAgentNote(body, action));

    Long tenantId = dev.getTenantId();
    if (tenantId == null) {
      log.warn("emqx webhook: device row has null tenant_id for device_key={}", deviceKey);
      return false;
    }

      try {
        DataDomainContext.setTenantId(tenantId);
        try {
          deviceService.createConnectRecord(dev.getProductKey(), dev.getDeviceKey(), req);
          publishConnectEvent(dev, action, body);
        } finally {
          DataDomainContext.clear();
        }
      } catch (MgmtException e) {
      log.warn("emqx webhook createConnectRecord failed: {}", e.getMessage());
    } catch (RuntimeException e) {
      log.warn("emqx webhook createConnectRecord error", e);
    }
    return true;
  }

  private void publishConnectEvent(AccessDeviceRecord dev, String action, EmqxClientSessionEvent body) {
    if (dev == null || dev.getTenantId() == null) {
      return;
    }
    String eventType = CONNECTED.equals(action)
        ? IotIntegrationEventType.CONNECT_CONNECTED
        : IotIntegrationEventType.CONNECT_DISCONNECTED;
    Map<String, String> subject = new LinkedHashMap<String, String>();
    subject.put("productKey", dev.getProductKey());
    subject.put("deviceKey", dev.getDeviceKey());
    Map<String, Object> data = new LinkedHashMap<String, Object>();
    data.put("action", action);
    if (body != null) {
      if (StringUtils.hasText(body.getClientid())) {
        data.put("clientid", body.getClientid());
      }
      if (StringUtils.hasText(body.getReason())) {
        data.put("reason", body.getReason());
      }
      if (StringUtils.hasText(body.getNode())) {
        data.put("node", body.getNode());
      }
      if (body.getTimestamp() != null) {
        data.put("emqxTimestamp", body.getTimestamp());
      }
    }
    long now = System.currentTimeMillis();
    normalizedEventPublisher.publish(NormalizedEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(eventType)
        .occurredAt(now)
        .tenantId(dev.getTenantId())
        .orgId(null)
        .source("aiot-service")
        .subject(subject)
        .data(data)
        .build());
  }

  private static String buildUserAgentNote(EmqxClientSessionEvent body, String action) {
    if (DISCONNECTED.equals(action) && StringUtils.hasText(body.getReason())) {
      String suffix = body.getNode() != null ? " node=" + body.getNode() : "";
      String note = "emqx-disconnect:" + body.getReason() + suffix;
      return trimToNull(note, 255);
    }
    if (StringUtils.hasText(body.getNode())) {
      return trimToNull("emqx:" + body.getNode(), 255);
    }
    return null;
  }

  private static String resolveAction(EmqxClientSessionEvent body) {
    String raw = safe(body.getEvent()).toLowerCase(Locale.ROOT);
    if (!raw.isEmpty()) {
      if (raw.contains("disconnect")) {
        return DISCONNECTED;
      }
      if (raw.contains("connect")) {
        return CONNECTED;
      }
    }
    if (StringUtils.hasText(body.getReason())) {
      return DISCONNECTED;
    }
    return CONNECTED;
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  static String parseIpFromPeername(String peername) {
    if (!StringUtils.hasText(peername)) {
      return "";
    }
    String p = peername.trim();
    if (p.startsWith("[") && p.contains("]:")) {
      int end = p.indexOf("]:");
      return p.substring(1, end);
    }
    int colon = p.lastIndexOf(':');
    if (colon > 0) {
      return p.substring(0, colon).trim();
    }
    return p;
  }

  private static String trimToNull(String s, int max) {
    if (!StringUtils.hasText(s)) {
      return null;
    }
    String t = s.trim();
    if (t.length() > max) {
      t = t.substring(0, max);
    }
    return t.isEmpty() ? null : t;
  }
}


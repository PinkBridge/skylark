package cn.skylark.aiot_service.iot.access.service;

import cn.skylark.aiot_service.iot.access.model.UpstreamIngestRequest;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceRecordMapper;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DeviceEventRecordEntity;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DevicePropertyRecordEntity;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DeviceServiceRecordEntity;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;

@Service
public class UpstreamIngestServiceImpl implements UpstreamIngestService {
  private static final Logger log = LoggerFactory.getLogger(UpstreamIngestServiceImpl.class);

  private final DeviceRecordMapper deviceRecordMapper;
  private final ObjectMapper objectMapper;

  public UpstreamIngestServiceImpl(DeviceRecordMapper deviceRecordMapper, ObjectMapper objectMapper) {
    this.deviceRecordMapper = deviceRecordMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public void ingest(UpstreamIngestRequest request) {
    if (request == null) return;
    String topic = safe(request.getTopic());
    if (!StringUtils.hasText(topic)) {
      log.warn("iot.upstream ignored: empty topic, traceId={}", request.getTraceId());
      return;
    }

    TopicDerived d = deriveFromAliTopic(topic);
    if (!StringUtils.hasText(d.productKey) || !StringUtils.hasText(d.deviceKey)) {
      log.warn("iot.upstream ignored: cannot derive product/device from topic={}, traceId={}", topic, request.getTraceId());
      return;
    }

    // tenant_id should be present via datadomain filter (from headers). If not, skip to avoid bad data.
    Long tenantId = DataDomainContext.getTenantId();
    if (tenantId == null) {
      log.warn("iot.upstream ignored: missing tenantId, topic={}, traceId={}", topic, request.getTraceId());
      return;
    }
    Long orgId = DataDomainContext.getOrgId();

    String payload = request.getPayload() == null ? "" : request.getPayload();
    long ts = request.getTimestamp() == null ? System.currentTimeMillis() : request.getTimestamp();

    if (d.type.equals("property_post")) {
      ingestPropertyPost(tenantId, orgId, d.productKey, d.deviceKey, topic, ts, request.getTraceId(), payload);
      return;
    }
    if (d.type.equals("event_post")) {
      DeviceEventRecordEntity e = new DeviceEventRecordEntity();
      e.setTenantId(tenantId);
      e.setOrgId(orgId);
      e.setProductKey(d.productKey);
      e.setDeviceName(d.deviceKey);
      e.setEventName(d.nameOrIdentifier);
      e.setTraceId(trimToNull(request.getTraceId()));
      e.setMessageId(extractMessageId(payload));
      e.setTopic(topic);
      e.setDeviceTimestamp(ts);
      e.setPayload(payload);
      deviceRecordMapper.insertEventRecord(e);
      return;
    }
    if (d.type.equals("service_reply")) {
      DeviceServiceRecordEntity s = new DeviceServiceRecordEntity();
      s.setTenantId(tenantId);
      s.setOrgId(orgId);
      s.setProductKey(d.productKey);
      s.setDeviceName(d.deviceKey);
      s.setServiceName(d.nameOrIdentifier);
      s.setDirection("reply");
      s.setTraceId(trimToNull(request.getTraceId()));
      s.setMessageId(extractMessageId(payload));
      s.setTopic(topic);
      s.setDeviceTimestamp(ts);
      s.setPayload(payload);
      deviceRecordMapper.insertServiceRecord(s);
      return;
    }

    // raw / unknown: store as event record for traceability
    DeviceEventRecordEntity e = new DeviceEventRecordEntity();
    e.setTenantId(tenantId);
    e.setOrgId(orgId);
    e.setProductKey(d.productKey);
    e.setDeviceName(d.deviceKey);
    e.setEventName("raw");
    e.setTraceId(trimToNull(request.getTraceId()));
    e.setMessageId(extractMessageId(payload));
    e.setTopic(topic);
    e.setDeviceTimestamp(ts);
    e.setPayload(payload);
    deviceRecordMapper.insertEventRecord(e);
  }

  private void ingestPropertyPost(Long tenantId, Long orgId, String productKey, String deviceKey,
                                  String topic, long ts, String traceId, String payload) {
    try {
      JsonNode root = objectMapper.readTree(payload == null ? "" : payload);
      JsonNode params = root.path("params");
      if (params != null && params.isObject()) {
        Iterator<Map.Entry<String, JsonNode>> it = params.fields();
        while (it.hasNext()) {
          Map.Entry<String, JsonNode> entry = it.next();
          String identifier = entry.getKey();
          JsonNode v = entry.getValue();
          DevicePropertyRecordEntity p = new DevicePropertyRecordEntity();
          p.setTenantId(tenantId);
          p.setOrgId(orgId);
          p.setProductKey(productKey);
          p.setDeviceName(deviceKey);
          p.setPropertyIdentifier(identifier);
          p.setPropertyValue(v == null ? null : (v.isTextual() ? v.asText() : v.toString()));
          p.setTraceId(trimToNull(traceId));
          p.setMessageId(extractMessageId(payload));
          p.setTopic(topic);
          p.setDeviceTimestamp(ts);
          p.setPayload(payload);
          deviceRecordMapper.insertPropertyRecord(p);
        }
        return;
      }
    } catch (Exception ignore) {
      // fallthrough
    }
    // fallback: unknown schema, store as a single record
    DevicePropertyRecordEntity p = new DevicePropertyRecordEntity();
    p.setTenantId(tenantId);
    p.setOrgId(orgId);
    p.setProductKey(productKey);
    p.setDeviceName(deviceKey);
    p.setPropertyIdentifier("raw");
    p.setPropertyValue(payload);
    p.setTraceId(trimToNull(traceId));
    p.setMessageId(extractMessageId(payload));
    p.setTopic(topic);
    p.setDeviceTimestamp(ts);
    p.setPayload(payload);
    deviceRecordMapper.insertPropertyRecord(p);
  }

  private String extractMessageId(String payload) {
    if (!StringUtils.hasText(payload)) return null;
    try {
      JsonNode root = objectMapper.readTree(payload);
      if (root.has("id")) {
        String id = root.path("id").asText(null);
        return trimToNull(id);
      }
      if (root.has("messageId")) {
        String id = root.path("messageId").asText(null);
        return trimToNull(id);
      }
    } catch (Exception ignore) {
      // ignore
    }
    return null;
  }

  private static class TopicDerived {
    final String productKey;
    final String deviceKey;
    final String type;
    final String nameOrIdentifier;

    private TopicDerived(String productKey, String deviceKey, String type, String nameOrIdentifier) {
      this.productKey = productKey;
      this.deviceKey = deviceKey;
      this.type = type;
      this.nameOrIdentifier = nameOrIdentifier;
    }
  }

  private static TopicDerived deriveFromAliTopic(String topic) {
    String[] parts = topic.split("/");
    String productKey = parts.length >= 3 ? parts[2] : "";
    String deviceKey = parts.length >= 4 ? parts[3] : "";
    String lower = topic.toLowerCase();
    if (lower.contains("thing/event/property/post")) {
      return new TopicDerived(productKey, deviceKey, "property_post", "property");
    }
    if (lower.contains("/thing/event/") && lower.endsWith("/post")) {
      String eventName = "event";
      int idx = lower.indexOf("/thing/event/");
      int end = lower.lastIndexOf("/post");
      if (idx >= 0 && end > idx + "/thing/event/".length()) {
        eventName = topic.substring(idx + "/thing/event/".length(), end);
      }
      return new TopicDerived(productKey, deviceKey, "event_post", eventName);
    }
    if (lower.contains("/thing/service/") && lower.endsWith("/reply")) {
      String serviceName = "service";
      int idx = lower.indexOf("/thing/service/");
      int end = lower.lastIndexOf("/reply");
      if (idx >= 0 && end > idx + "/thing/service/".length()) {
        serviceName = topic.substring(idx + "/thing/service/".length(), end);
      }
      return new TopicDerived(productKey, deviceKey, "service_reply", serviceName);
    }
    return new TopicDerived(productKey, deviceKey, "raw", "raw");
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private static String trimToNull(String s) {
    if (!StringUtils.hasText(s)) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}


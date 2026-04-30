package cn.skylark.aiot_service.iot.alarm;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmEntity;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmEvalStateEntity;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmRuleEntity;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmEvalStateMapper;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmMapper;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmRuleMapper;
import cn.skylark.aiot_service.iot.appint.OutboundDispatchService;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceGroupRelMapper;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceMapper;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DeviceEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlarmEvaluatorService {
  private final IotAlarmRuleMapper ruleMapper;
  private final IotAlarmMapper alarmMapper;
  private final IotAlarmEvalStateMapper stateMapper;
  private final DeviceGroupRelMapper deviceGroupRelMapper;
  private final DeviceMapper deviceMapper;
  private final OutboundDispatchService outboundDispatchService;
  private final ObjectMapper objectMapper;

  public AlarmEvaluatorService(IotAlarmRuleMapper ruleMapper,
                               IotAlarmMapper alarmMapper,
                               IotAlarmEvalStateMapper stateMapper,
                               DeviceGroupRelMapper deviceGroupRelMapper,
                               DeviceMapper deviceMapper,
                               OutboundDispatchService outboundDispatchService,
                               ObjectMapper objectMapper) {
    this.ruleMapper = ruleMapper;
    this.alarmMapper = alarmMapper;
    this.stateMapper = stateMapper;
    this.deviceGroupRelMapper = deviceGroupRelMapper;
    this.deviceMapper = deviceMapper;
    this.outboundDispatchService = outboundDispatchService;
    this.objectMapper = objectMapper;
  }

  public void evaluate(NormalizedEvent event) {
    if (event == null || event.getTenantId() == null) {
      return;
    }
    // Avoid recursive evaluation of emitted alarm events.
    if (event.getEventType() != null && event.getEventType().startsWith("alarm.")) {
      return;
    }
    String productKey = subject(event, "productKey");
    String deviceKey = subject(event, "deviceKey");
    if (!StringUtils.hasText(productKey) || !StringUtils.hasText(deviceKey)) {
      return;
    }

    List<String> groupKeys = deviceGroupRelMapper.listGroupKeysByDevice(event.getTenantId(), productKey, deviceKey);
    if (groupKeys == null || groupKeys.isEmpty()) {
      return;
    }
    List<IotAlarmRuleEntity> rules = ruleMapper.listEnabledByGroupKeys(event.getTenantId(), groupKeys);
    if (rules == null || rules.isEmpty()) {
      return;
    }

    DeviceEntity device = deviceMapper.findByPkAndDeviceKey(productKey, deviceKey);
    for (IotAlarmRuleEntity rule : rules) {
      if (rule == null) continue;
      // tenant check (defensive)
      if (rule.getTenantId() != null && !rule.getTenantId().equals(event.getTenantId())) {
        continue;
      }
      evaluateOne(rule, event, productKey, deviceKey, device);
    }
  }

  private void evaluateOne(IotAlarmRuleEntity rule, NormalizedEvent event, String productKey, String deviceKey, DeviceEntity device) {
    String sourceType = upper(rule.getSourceType());
    boolean met;
    Map<String, Object> evidence = new LinkedHashMap<String, Object>();
    evidence.put("ruleId", rule.getId());
    evidence.put("deviceGroupKey", rule.getDeviceGroupKey());
    evidence.put("sourceType", sourceType);
    evidence.put("severity", upper(rule.getSeverity()));

    if ("PROPERTY".equals(sourceType)) {
      if (!"device.property.reported".equals(event.getEventType())) {
        return;
      }
      PropertyEvalResult r = evalProperty(rule.getConditionJson(), event);
      if (!r.valid) {
        return;
      }
      met = r.met;
      evidence.putAll(r.evidence);
    } else if ("EVENT".equals(sourceType)) {
      EventEvalResult r = evalEvent(rule.getConditionJson(), event);
      if (!r.valid) {
        return;
      }
      met = r.met;
      evidence.putAll(r.evidence);
    } else {
      return;
    }

    String triggerMode = upper(rule.getTriggerMode());
    int durationSeconds = rule.getDurationSeconds() == null ? 0 : rule.getDurationSeconds();
    String recoveryMode = upper(rule.getRecoveryMode());
    String dedupMode = upper(rule.getDedupMode());
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

    boolean shouldTrigger = false;
    boolean shouldRecover = false;

    if ("DURATION".equals(triggerMode)) {
      IotAlarmEvalStateEntity st = stateMapper.findByRuleAndDevice(event.getTenantId(), rule.getId(), productKey, deviceKey);
      if (st == null) {
        st = new IotAlarmEvalStateEntity();
        st.setTenantId(event.getTenantId());
        st.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
        st.setRuleId(rule.getId());
        st.setDeviceGroupKey(rule.getDeviceGroupKey());
        st.setProductKey(productKey);
        st.setDeviceKey(deviceKey);
        st.setConditionMetSince(null);
        st.setLastSeenAt(now);
        st.setCreateUser(device != null ? device.getCreateUser() : null);
        st.setUpdateUser(device != null ? device.getUpdateUser() : null);
        stateMapper.insert(st);
      }

      if (met) {
        if (st.getConditionMetSince() == null) {
          st.setConditionMetSince(now);
          st.setLastSeenAt(now);
          st.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
          st.setUpdateUser(device != null ? device.getUpdateUser() : null);
          stateMapper.update(st);
        } else {
          st.setLastSeenAt(now);
          st.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
          st.setUpdateUser(device != null ? device.getUpdateUser() : null);
          stateMapper.update(st);
          long sec = java.time.Duration.between(st.getConditionMetSince(), now).getSeconds();
          if (sec >= Math.max(durationSeconds, 1)) {
            shouldTrigger = true;
          }
        }
      } else {
        // condition broken
        if (st.getConditionMetSince() != null) {
          st.setConditionMetSince(null);
          st.setLastSeenAt(now);
          st.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
          st.setUpdateUser(device != null ? device.getUpdateUser() : null);
          stateMapper.update(st);
        }
        if ("AUTO".equals(recoveryMode)) {
          shouldRecover = true;
        }
      }
    } else {
      // INSTANT
      if (met) {
        shouldTrigger = true;
      } else if ("AUTO".equals(recoveryMode)) {
        shouldRecover = true;
      }
    }

    if (shouldTrigger) {
      triggerAlarm(rule, event, productKey, deviceKey, device, dedupMode, recoveryMode, evidence, now);
    } else if (shouldRecover) {
      recoverAlarm(rule, event, productKey, deviceKey, device, evidence, now);
    }
  }

  private void triggerAlarm(IotAlarmRuleEntity rule,
                            NormalizedEvent event,
                            String productKey,
                            String deviceKey,
                            DeviceEntity device,
                            String dedupMode,
                            String recoveryMode,
                            Map<String, Object> evidence,
                            LocalDateTime now) {
    IotAlarmEntity active = null;
    if ("SINGLE_ACTIVE".equals(dedupMode)) {
      active = alarmMapper.findActiveByRuleAndDevice(event.getTenantId(), rule.getId(), productKey, deviceKey);
    }

    if (active == null) {
      boolean autoRecover = "AUTO".equals(recoveryMode) && !"SINGLE_ACTIVE".equals(dedupMode);
      IotAlarmEntity a = new IotAlarmEntity();
      a.setTenantId(event.getTenantId());
      a.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
      a.setRuleId(rule.getId());
      a.setDeviceGroupKey(rule.getDeviceGroupKey());
      a.setProductKey(productKey);
      a.setDeviceKey(deviceKey);
      a.setSeverity(upper(rule.getSeverity()));
      a.setStatus(autoRecover ? "RECOVERED" : "ACTIVE");
      a.setFirstTriggeredAt(now);
      a.setLastTriggeredAt(now);
      a.setRecoveredAt(autoRecover ? now : null);
      a.setTriggerCount(1);
      a.setEvidenceJson(writeJson(evidence));
      a.setLastEventId(event.getEventId());
      a.setLastEventType(event.getEventType());
      a.setCreateUser(device != null ? device.getCreateUser() : null);
      a.setUpdateUser(device != null ? device.getUpdateUser() : null);
      alarmMapper.insert(a);
      emitAlarmEvent("alarm.triggered", rule, event, productKey, deviceKey, evidence);
      if (autoRecover) {
        emitAlarmEvent("alarm.recovered", rule, event, productKey, deviceKey, evidence);
      }
      return;
    }

    active.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
    active.setLastTriggeredAt(now);
    active.setRecoveredAt(null);
    active.setStatus("ACTIVE");
    active.setTriggerCount((active.getTriggerCount() == null ? 0 : active.getTriggerCount()) + 1);
    active.setEvidenceJson(writeJson(evidence));
    active.setLastEventId(event.getEventId());
    active.setLastEventType(event.getEventType());
    active.setUpdateUser(device != null ? device.getUpdateUser() : null);
    alarmMapper.update(active);
    // For SINGLE_ACTIVE, we still emit triggered event on each match? MVP: emit only on first trigger.
  }

  private void recoverAlarm(IotAlarmRuleEntity rule,
                            NormalizedEvent event,
                            String productKey,
                            String deviceKey,
                            DeviceEntity device,
                            Map<String, Object> evidence,
                            LocalDateTime now) {
    IotAlarmEntity active = alarmMapper.findActiveByRuleAndDevice(event.getTenantId(), rule.getId(), productKey, deviceKey);
    if (active == null) {
      return;
    }
    active.setOrgId(device != null ? device.getOrgId() : event.getOrgId());
    active.setStatus("RECOVERED");
    active.setRecoveredAt(now);
    active.setLastTriggeredAt(active.getLastTriggeredAt() == null ? now : active.getLastTriggeredAt());
    active.setEvidenceJson(writeJson(evidence));
    active.setLastEventId(event.getEventId());
    active.setLastEventType(event.getEventType());
    active.setUpdateUser(device != null ? device.getUpdateUser() : null);
    alarmMapper.update(active);
    emitAlarmEvent("alarm.recovered", rule, event, productKey, deviceKey, evidence);
  }

  private void emitAlarmEvent(String alarmEventType,
                              IotAlarmRuleEntity rule,
                              NormalizedEvent triggerEvent,
                              String productKey,
                              String deviceKey,
                              Map<String, Object> evidence) {
    Map<String, String> subject = new LinkedHashMap<String, String>();
    subject.put("productKey", productKey);
    subject.put("deviceKey", deviceKey);
    subject.put("deviceGroupKey", rule.getDeviceGroupKey());
    subject.put("ruleId", String.valueOf(rule.getId()));
    Map<String, Object> data = new LinkedHashMap<String, Object>();
    data.put("severity", upper(rule.getSeverity()));
    data.put("ruleName", rule.getName());
    data.put("sourceType", upper(rule.getSourceType()));
    data.put("triggerMode", upper(rule.getTriggerMode()));
    data.put("durationSeconds", rule.getDurationSeconds());
    data.put("recoveryMode", upper(rule.getRecoveryMode()));
    data.put("dedupMode", upper(rule.getDedupMode()));
    data.put("evidence", evidence);
    if (triggerEvent != null) {
      data.put("upstreamEventId", triggerEvent.getEventId());
      data.put("upstreamEventType", triggerEvent.getEventType());
    }

    NormalizedEvent alarmEvent = NormalizedEvent.builder()
        .eventId(UUID.randomUUID().toString())
        .eventType(alarmEventType)
        .occurredAt(System.currentTimeMillis())
        .tenantId(triggerEvent.getTenantId())
        .orgId(triggerEvent.getOrgId())
        .source("aiot-service")
        .subject(subject)
        .data(data)
        .build();
    outboundDispatchService.dispatch(alarmEvent);
  }

  private PropertyEvalResult evalProperty(String conditionJson, NormalizedEvent event) {
    if (!StringUtils.hasText(conditionJson) || event.getData() == null) {
      return PropertyEvalResult.invalid();
    }
    try {
      JsonNode c = objectMapper.readTree(conditionJson);
      String propertyKey = text(c, "propertyKey");
      String operator = upper(text(c, "operator"));
      JsonNode threshold = c.get("threshold");
      if (!StringUtils.hasText(propertyKey) || !StringUtils.hasText(operator) || threshold == null) {
        return PropertyEvalResult.invalid();
      }
      Object propsObj = event.getData().get("properties");
      if (!(propsObj instanceof Map)) {
        return PropertyEvalResult.invalid();
      }
      Map<?, ?> props = (Map<?, ?>) propsObj;
      Object raw = props.get(propertyKey);
      Double v = toNumber(raw);
      if (v == null) {
        return PropertyEvalResult.invalid();
      }
      boolean met = compareNumber(v, operator, threshold);

      Map<String, Object> evidence = new LinkedHashMap<String, Object>();
      evidence.put("propertyKey", propertyKey);
      evidence.put("operator", operator);
      evidence.put("value", v);
      evidence.put("threshold", objectMapper.convertValue(threshold, Object.class));
      return new PropertyEvalResult(true, met, evidence);
    } catch (Exception e) {
      return PropertyEvalResult.invalid();
    }
  }

  private EventEvalResult evalEvent(String conditionJson, NormalizedEvent event) {
    if (!StringUtils.hasText(conditionJson) || event == null) {
      return EventEvalResult.invalid();
    }
    try {
      JsonNode c = objectMapper.readTree(conditionJson);
      String eventType = text(c, "eventType");
      String eventIdentifier = text(c, "eventIdentifier");
      if (StringUtils.hasText(eventType) && !eventType.equals(event.getEventType())) {
        return new EventEvalResult(true, false, new LinkedHashMap<String, Object>());
      }
      if (StringUtils.hasText(eventIdentifier)) {
        Object ei = event.getData() == null ? null : event.getData().get("eventIdentifier");
        if (ei == null || !eventIdentifier.equals(String.valueOf(ei))) {
          return new EventEvalResult(true, false, new LinkedHashMap<String, Object>());
        }
      }
      Map<String, Object> evidence = new LinkedHashMap<String, Object>();
      evidence.put("eventType", event.getEventType());
      if (StringUtils.hasText(eventIdentifier)) {
        evidence.put("eventIdentifier", eventIdentifier);
      }
      return new EventEvalResult(true, true, evidence);
    } catch (Exception e) {
      return EventEvalResult.invalid();
    }
  }

  private boolean compareNumber(double value, String operator, JsonNode threshold) {
    switch (operator) {
      case "GT":
        return value > threshold.path("value").asDouble();
      case "GTE":
        return value >= threshold.path("value").asDouble();
      case "LT":
        return value < threshold.path("value").asDouble();
      case "LTE":
        return value <= threshold.path("value").asDouble();
      case "EQ":
        return value == threshold.path("value").asDouble();
      case "NE":
        return value != threshold.path("value").asDouble();
      case "BETWEEN":
        double min = threshold.path("min").asDouble();
        double max = threshold.path("max").asDouble();
        boolean includeMin = threshold.path("includeMin").asBoolean(true);
        boolean includeMax = threshold.path("includeMax").asBoolean(true);
        boolean okMin = includeMin ? value >= min : value > min;
        boolean okMax = includeMax ? value <= max : value < max;
        return okMin && okMax;
      default:
        return false;
    }
  }

  private static Double toNumber(Object raw) {
    if (raw == null) return null;
    if (raw instanceof Number) {
      return ((Number) raw).doubleValue();
    }
    try {
      String s = String.valueOf(raw).trim();
      if (!StringUtils.hasText(s)) return null;
      return Double.parseDouble(s);
    } catch (Exception e) {
      return null;
    }
  }

  private static String subject(NormalizedEvent event, String key) {
    if (event == null || event.getSubject() == null) return null;
    return event.getSubject().get(key);
  }

  private static String upper(String s) {
    return s == null ? "" : s.trim().toUpperCase();
  }

  private static String text(JsonNode n, String field) {
    if (n == null) return null;
    JsonNode v = n.get(field);
    if (v == null || !v.isTextual()) return null;
    String t = v.asText();
    return StringUtils.hasText(t) ? t.trim() : null;
  }

  private String writeJson(Object obj) {
    try {
      if (obj == null) return null;
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      return null;
    }
  }

  private static final class PropertyEvalResult {
    final boolean valid;
    final boolean met;
    final Map<String, Object> evidence;

    PropertyEvalResult(boolean valid, boolean met, Map<String, Object> evidence) {
      this.valid = valid;
      this.met = met;
      this.evidence = evidence;
    }

    static PropertyEvalResult invalid() {
      return new PropertyEvalResult(false, false, new LinkedHashMap<String, Object>());
    }
  }

  private static final class EventEvalResult {
    final boolean valid;
    final boolean met;
    final Map<String, Object> evidence;

    EventEvalResult(boolean valid, boolean met, Map<String, Object> evidence) {
      this.valid = valid;
      this.met = met;
      this.evidence = evidence;
    }

    static EventEvalResult invalid() {
      return new EventEvalResult(false, false, new LinkedHashMap<String, Object>());
    }
  }
}


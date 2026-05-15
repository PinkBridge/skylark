package cn.skylark.aiot_service.iot.alarm.service;

import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmNotifyConfigEntity;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmNotifyDeliveryEntity;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmNotifyConfigMapper;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmNotifyDeliveryMapper;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.aiot_service.iot.notify.entity.IotNotifyChannelEntity;
import cn.skylark.aiot_service.iot.notify.mapper.IotNotifyChannelMapper;
import cn.skylark.aiot_service.iot.notify.service.NotifyChannelSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlarmNotifyDispatchService {
  private static final Logger log = LoggerFactory.getLogger(AlarmNotifyDispatchService.class);
  public static final String CH_EMAIL = "EMAIL";
  public static final String CH_SMS = "SMS";
  public static final String ST_SUCCESS = "SUCCESS";
  public static final String ST_FAILED = "FAILED";

  private final IotAlarmNotifyConfigMapper notifyConfigMapper;
  private final IotAlarmNotifyDeliveryMapper deliveryMapper;
  private final IotNotifyChannelMapper notifyChannelMapper;
  private final NotifyChannelSender notifyChannelSender;
  private final ObjectMapper objectMapper;

  public AlarmNotifyDispatchService(IotAlarmNotifyConfigMapper notifyConfigMapper,
                                    IotAlarmNotifyDeliveryMapper deliveryMapper,
                                    IotNotifyChannelMapper notifyChannelMapper,
                                    NotifyChannelSender notifyChannelSender,
                                    ObjectMapper objectMapper) {
    this.notifyConfigMapper = notifyConfigMapper;
    this.deliveryMapper = deliveryMapper;
    this.notifyChannelMapper = notifyChannelMapper;
    this.notifyChannelSender = notifyChannelSender;
    this.objectMapper = objectMapper;
  }

  public void dispatchAlarmEvent(NormalizedEvent event) {
    if (event == null || event.getTenantId() == null) {
      return;
    }
    String type = event.getEventType();
    if (!"alarm.triggered".equals(type) && !"alarm.recovered".equals(type)) {
      return;
    }
    Long ruleId = parseRuleId(event);
    if (ruleId == null) {
      return;
    }
    Long tenantId = event.getTenantId();
    List<IotAlarmNotifyConfigEntity> configs = notifyConfigMapper.listByTenantAndRule(tenantId, ruleId, 1);
    if (configs == null || configs.isEmpty()) {
      return;
    }
    for (IotAlarmNotifyConfigEntity cfg : configs) {
      if (cfg.getEnabled() == null || cfg.getEnabled() == 0) {
        continue;
      }
      if (cfg.getEmailEnabled() != null && cfg.getEmailEnabled() != 0) {
        IotNotifyChannelEntity ch = loadChannel(cfg.getEmailNotifyChannelId(), tenantId, "EMAIL");
        sendEmailChannel(event, cfg, ch, false);
      }
      if (cfg.getSmsEnabled() != null && cfg.getSmsEnabled() != 0) {
        IotNotifyChannelEntity ch = loadChannel(cfg.getSmsNotifyChannelId(), tenantId, "SMS");
        sendSmsChannel(event, cfg, ch, false);
      }
    }
  }

  public void dispatchTest(IotAlarmNotifyConfigEntity cfg) {
    NormalizedEvent fake = NormalizedEvent.builder()
        .eventId("test-" + System.currentTimeMillis())
        .eventType("alarm.test")
        .occurredAt(System.currentTimeMillis())
        .tenantId(cfg.getTenantId())
        .orgId(cfg.getOrgId())
        .source("aiot-service")
        .subject(Collections.singletonMap("ruleId", String.valueOf(cfg.getRuleId())))
        .data(Collections.singletonMap("ruleName", String.valueOf(cfg.getName())))
        .build();
    if (cfg.getEmailEnabled() != null && cfg.getEmailEnabled() != 0) {
      IotNotifyChannelEntity ch = loadChannel(cfg.getEmailNotifyChannelId(), cfg.getTenantId(), "EMAIL");
      sendEmailChannel(fake, cfg, ch, true);
    }
    if (cfg.getSmsEnabled() != null && cfg.getSmsEnabled() != 0) {
      IotNotifyChannelEntity ch = loadChannel(cfg.getSmsNotifyChannelId(), cfg.getTenantId(), "SMS");
      sendSmsChannel(fake, cfg, ch, true);
    }
  }

  private IotNotifyChannelEntity loadChannel(Long id, Long tenantId, String expectedKind) {
    if (id == null) {
      return null;
    }
    IotNotifyChannelEntity e = notifyChannelMapper.findById(id);
    if (e == null || e.getTenantId() == null || !e.getTenantId().equals(tenantId)) {
      return null;
    }
    if (!expectedKind.equalsIgnoreCase(e.getChannelKind())) {
      return null;
    }
    if (e.getEnabled() == null || e.getEnabled() == 0) {
      return null;
    }
    return e;
  }

  private void sendEmailChannel(NormalizedEvent event,
                                IotAlarmNotifyConfigEntity cfg,
                                IotNotifyChannelEntity channel,
                                boolean test) {
    List<String> recipients = splitAddresses(cfg.getToEmails());
    String hint = summarizeList(recipients, 400);
    if (channel == null) {
      insertDelivery(cfg, event, CH_EMAIL, ST_FAILED, null, null,
          "email notify channel not selected or invalid", hint, test);
      return;
    }
    if (recipients.isEmpty()) {
      insertDelivery(cfg, event, CH_EMAIL, ST_FAILED, null, null, "no email recipients", hint, test);
      return;
    }
    String subject = buildMailSubject(event);
    String body = buildMailBody(event);
    boolean allOk = true;
    String lastErr = null;
    for (String to : recipients) {
      try {
        notifyChannelSender.sendMailFromChannel(channel, to, subject, body);
      } catch (MgmtException e) {
        allOk = false;
        lastErr = e.getMessage();
        log.debug("alarm notify mail failed to={}", to, e);
      } catch (RuntimeException e) {
        allOk = false;
        lastErr = e.getMessage();
        log.warn("alarm notify mail failed to={}", to, e);
      }
    }
    if (!allOk) {
      insertDelivery(cfg, event, CH_EMAIL, ST_FAILED, null, HttpStatus.BAD_REQUEST.value(), lastErr, hint, test);
    } else {
      insertDelivery(cfg, event, CH_EMAIL, ST_SUCCESS, null, null, null, hint, test);
    }
  }

  private void sendSmsChannel(NormalizedEvent event,
                              IotAlarmNotifyConfigEntity cfg,
                              IotNotifyChannelEntity channel,
                              boolean test) {
    List<String> phones = splitAddresses(cfg.getToMobiles());
    String hint = summarizeList(phones, 400);
    if (channel == null) {
      insertDelivery(cfg, event, CH_SMS, ST_FAILED, null, null,
          "sms notify channel not selected or invalid", hint, test);
      return;
    }
    if (phones.isEmpty()) {
      insertDelivery(cfg, event, CH_SMS, ST_FAILED, null, null, "no sms recipients", hint, test);
      return;
    }
    String paramJson = smsTemplateJson(event);
    boolean allOk = true;
    String lastErr = null;
    for (String phone : phones) {
      try {
        notifyChannelSender.sendSmsFromChannel(channel, phone, paramJson);
      } catch (MgmtException e) {
        allOk = false;
        lastErr = e.getMessage();
      } catch (RuntimeException e) {
        allOk = false;
        lastErr = e.getMessage();
        log.warn("alarm notify sms failed phone={}", phone, e);
      }
    }
    if (!allOk) {
      insertDelivery(cfg, event, CH_SMS, ST_FAILED, null, HttpStatus.BAD_REQUEST.value(), lastErr, hint, test);
    } else {
      insertDelivery(cfg, event, CH_SMS, ST_SUCCESS, null, null, null, hint, test);
    }
  }

  private void insertDelivery(IotAlarmNotifyConfigEntity cfg,
                              NormalizedEvent event,
                              String channel,
                              String status,
                              String vendorCode,
                              Integer httpStatus,
                              String err,
                              String recipientHint,
                              boolean test) {
    IotAlarmNotifyDeliveryEntity d = new IotAlarmNotifyDeliveryEntity();
    d.setTenantId(cfg.getTenantId());
    d.setOrgId(cfg.getOrgId());
    d.setNotifyConfigId(cfg.getId());
    d.setRuleId(cfg.getRuleId());
    d.setAlarmEventId(event.getEventId());
    d.setAlarmEventType(event.getEventType());
    d.setChannel(channel);
    d.setStatus(status);
    d.setVendorCode(vendorCode);
    d.setHttpStatus(httpStatus);
    d.setErrorMessage(err == null ? null : truncate(err, 1000));
    d.setRecipientHint(recipientHint == null ? null : truncate(recipientHint, 500));
    d.setIsTest(test ? 1 : 0);
    deliveryMapper.insert(d);
  }

  private String buildMailSubject(NormalizedEvent event) {
    String ruleName = stringData(event, "ruleName");
    String type = event.getEventType();
    return "[IoT Alarm] " + (StringUtils.hasText(ruleName) ? ruleName : "rule") + " — " + type;
  }

  private String buildMailBody(NormalizedEvent event) {
    StringBuilder sb = new StringBuilder();
    sb.append("Event: ").append(event.getEventType()).append("\n");
    sb.append("Rule: ").append(stringData(event, "ruleName")).append("\n");
    sb.append("Severity: ").append(stringData(event, "severity")).append("\n");
    if (event.getSubject() != null) {
      sb.append("Product: ").append(nullSafe(event.getSubject().get("productKey"))).append("\n");
      sb.append("Device: ").append(nullSafe(event.getSubject().get("deviceKey"))).append("\n");
      sb.append("Group: ").append(nullSafe(event.getSubject().get("deviceGroupKey"))).append("\n");
    }
    if (event.getData() != null && event.getData().get("evidence") != null) {
      try {
        String ev = objectMapper.writeValueAsString(event.getData().get("evidence"));
        sb.append("Evidence:\n").append(truncate(ev, 4000)).append("\n");
      } catch (Exception e) {
        sb.append("Evidence: (unserializable)\n");
      }
    }
    return sb.toString();
  }

  private String smsTemplateJson(NormalizedEvent event) {
    String rule = stringData(event, "ruleName");
    String line = (StringUtils.hasText(rule) ? rule : "Alarm") + " | " + event.getEventType();
    Map<String, Object> m = new LinkedHashMap<String, Object>();
    m.put("content", line);
    try {
      return objectMapper.writeValueAsString(m);
    } catch (Exception e) {
      return "{\"content\":\"Alarm\"}";
    }
  }

  private static String stringData(NormalizedEvent event, String key) {
    if (event.getData() == null) {
      return "";
    }
    Object v = event.getData().get(key);
    return v == null ? "" : String.valueOf(v);
  }

  private static String nullSafe(String s) {
    return s == null ? "" : s;
  }

  private static Long parseRuleId(NormalizedEvent event) {
    if (event.getSubject() == null) {
      return null;
    }
    String raw = event.getSubject().get("ruleId");
    if (!StringUtils.hasText(raw)) {
      return null;
    }
    try {
      return Long.parseLong(raw.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static List<String> splitAddresses(String raw) {
    if (!StringUtils.hasText(raw)) {
      return Collections.emptyList();
    }
    String[] parts = raw.split("[,;\\s\\n\\r]+");
    List<String> out = new ArrayList<String>();
    for (String p : parts) {
      if (StringUtils.hasText(p)) {
        out.add(p.trim());
      }
    }
    return out;
  }

  private static String summarizeList(List<String> items, int maxLen) {
    if (items == null || items.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (String s : items) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(s);
      if (sb.length() >= maxLen) {
        sb.append("…");
        break;
      }
    }
    return sb.toString();
  }

  private static String truncate(String s, int max) {
    if (s == null || s.length() <= max) {
      return s;
    }
    return s.substring(0, max - 1) + "…";
  }
}

package cn.skylark.aiot_service.iot.alarm.service;

import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigCreateRequest;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigPageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigPageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyConfigUpdateRequest;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyDeliveryPageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyDeliveryPageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmNotifyDeliveryResponse;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmNotifyConfigEntity;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmNotifyDeliveryEntity;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmRuleEntity;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmNotifyConfigMapper;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmNotifyDeliveryMapper;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmRuleMapper;
import cn.skylark.aiot_service.iot.mgmt.service.AiotDataDomainSupport;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.aiot_service.iot.notify.entity.IotNotifyChannelEntity;
import cn.skylark.aiot_service.iot.notify.mapper.IotNotifyChannelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlarmNotifyMgmtService {
  private final IotAlarmNotifyConfigMapper notifyConfigMapper;
  private final IotAlarmNotifyDeliveryMapper deliveryMapper;
  private final IotAlarmRuleMapper ruleMapper;
  private final IotNotifyChannelMapper notifyChannelMapper;
  private final AlarmNotifyDispatchService alarmNotifyDispatchService;

  public AlarmNotifyMgmtService(IotAlarmNotifyConfigMapper notifyConfigMapper,
                                IotAlarmNotifyDeliveryMapper deliveryMapper,
                                IotAlarmRuleMapper ruleMapper,
                                IotNotifyChannelMapper notifyChannelMapper,
                                AlarmNotifyDispatchService alarmNotifyDispatchService) {
    this.notifyConfigMapper = notifyConfigMapper;
    this.deliveryMapper = deliveryMapper;
    this.ruleMapper = ruleMapper;
    this.notifyChannelMapper = notifyChannelMapper;
    this.alarmNotifyDispatchService = alarmNotifyDispatchService;
  }

  public AlarmNotifyConfigPageResponse listNotifyConfigs(AlarmNotifyConfigPageQuery query) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    AlarmNotifyConfigPageResponse resp = new AlarmNotifyConfigPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(notifyConfigMapper.countPage(tenantId, query.getRuleId()));
    List<IotAlarmNotifyConfigEntity> rows = notifyConfigMapper.listPage(tenantId, query.getRuleId(), offset, pageSize);
    Map<Long, String> ruleNameById = new HashMap<Long, String>();
    List<AlarmNotifyConfigResponse> records = new ArrayList<AlarmNotifyConfigResponse>();
    if (rows != null) {
      for (IotAlarmNotifyConfigEntity e : rows) {
        records.add(toConfigResponse(e, ruleNameById));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public AlarmNotifyConfigResponse getNotifyConfig(Long id) {
    IotAlarmNotifyConfigEntity e = requireConfig(id);
    return toConfigResponse(e, new HashMap<Long, String>());
  }

  public AlarmNotifyConfigResponse createNotifyConfig(AlarmNotifyConfigCreateRequest req) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotAlarmRuleEntity rule = requireRuleForTenant(req.getRuleId(), tenantId);
    validateChannelBindings(tenantId, req.getEmailEnabled(), req.getSmsEnabled(),
        req.getEmailNotifyChannelId(), req.getSmsNotifyChannelId());
    IotAlarmNotifyConfigEntity e = new IotAlarmNotifyConfigEntity();
    e.setTenantId(tenantId);
    e.setOrgId(AiotDataDomainSupport.currentOrgId());
    e.setRuleId(rule.getId());
    e.setName(req.getName().trim());
    e.setRemark(trimOrNull(req.getRemark()));
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setEmailEnabled(req.getEmailEnabled() ? 1 : 0);
    e.setSmsEnabled(req.getSmsEnabled() ? 1 : 0);
    e.setEmailNotifyChannelId(Boolean.TRUE.equals(req.getEmailEnabled()) ? req.getEmailNotifyChannelId() : null);
    e.setSmsNotifyChannelId(Boolean.TRUE.equals(req.getSmsEnabled()) ? req.getSmsNotifyChannelId() : null);
    e.setToEmails(trimOrNull(req.getToEmails()));
    e.setToMobiles(trimOrNull(req.getToMobiles()));
    notifyConfigMapper.insert(e);
    return getNotifyConfig(e.getId());
  }

  public AlarmNotifyConfigResponse updateNotifyConfig(Long id, AlarmNotifyConfigUpdateRequest req) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotAlarmNotifyConfigEntity e = requireConfig(id);
    validateChannelBindings(tenantId, req.getEmailEnabled(), req.getSmsEnabled(),
        req.getEmailNotifyChannelId(), req.getSmsNotifyChannelId());
    e.setName(req.getName().trim());
    e.setRemark(trimOrNull(req.getRemark()));
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setEmailEnabled(req.getEmailEnabled() ? 1 : 0);
    e.setSmsEnabled(req.getSmsEnabled() ? 1 : 0);
    e.setEmailNotifyChannelId(Boolean.TRUE.equals(req.getEmailEnabled()) ? req.getEmailNotifyChannelId() : null);
    e.setSmsNotifyChannelId(Boolean.TRUE.equals(req.getSmsEnabled()) ? req.getSmsNotifyChannelId() : null);
    e.setToEmails(trimOrNull(req.getToEmails()));
    e.setToMobiles(trimOrNull(req.getToMobiles()));
    if (notifyConfigMapper.update(e) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "notify config not found");
    }
    return getNotifyConfig(id);
  }

  public void deleteNotifyConfig(Long id) {
    requireConfig(id);
    if (notifyConfigMapper.softDeleteById(id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "notify config not found");
    }
  }

  public void testNotifyConfig(Long id) {
    IotAlarmNotifyConfigEntity cfg = requireConfig(id);
    boolean wantEmail = cfg.getEmailEnabled() != null && cfg.getEmailEnabled() != 0;
    boolean wantSms = cfg.getSmsEnabled() != null && cfg.getSmsEnabled() != 0;
    if (!wantEmail && !wantSms) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "enable at least one channel on this config");
    }
    alarmNotifyDispatchService.dispatchTest(cfg);
  }

  public AlarmNotifyDeliveryPageResponse listDeliveries(AlarmNotifyDeliveryPageQuery query) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    String ch = trimOrNull(query.getChannel());
    String st = trimOrNull(query.getStatus());
    AlarmNotifyDeliveryPageResponse resp = new AlarmNotifyDeliveryPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(deliveryMapper.countPage(tenantId, query.getRuleId(), query.getNotifyConfigId(), ch, st));
    List<IotAlarmNotifyDeliveryEntity> rows =
        deliveryMapper.listPage(tenantId, query.getRuleId(), query.getNotifyConfigId(), ch, st, offset, pageSize);
    List<AlarmNotifyDeliveryResponse> records = new ArrayList<AlarmNotifyDeliveryResponse>();
    if (rows != null) {
      for (IotAlarmNotifyDeliveryEntity row : rows) {
        records.add(toDeliveryResponse(row));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  private void validateChannelBindings(Long tenantId,
                                       Boolean emailEnabled,
                                       Boolean smsEnabled,
                                       Long emailChannelId,
                                       Long smsChannelId) {
    if (Boolean.TRUE.equals(emailEnabled)) {
      if (emailChannelId == null) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "emailNotifyChannelId required when email is enabled");
      }
      requireChannelKind(tenantId, emailChannelId, "EMAIL");
    }
    if (Boolean.TRUE.equals(smsEnabled)) {
      if (smsChannelId == null) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "smsNotifyChannelId required when sms is enabled");
      }
      requireChannelKind(tenantId, smsChannelId, "SMS");
    }
  }

  private void requireChannelKind(Long tenantId, Long channelId, String kind) {
    IotNotifyChannelEntity ch = notifyChannelMapper.findById(channelId);
    if (ch == null || ch.getTenantId() == null || !ch.getTenantId().equals(tenantId)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "notify channel not found");
    }
    if (!kind.equalsIgnoreCase(ch.getChannelKind())) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "notify channel kind mismatch");
    }
    if (ch.getEnabled() == null || ch.getEnabled() == 0) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "notify channel is disabled");
    }
  }

  private IotAlarmNotifyConfigEntity requireConfig(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotAlarmNotifyConfigEntity e = notifyConfigMapper.findById(id);
    if (e == null || e.getTenantId() == null || !e.getTenantId().equals(tenantId)) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "notify config not found");
    }
    return e;
  }

  private IotAlarmRuleEntity requireRuleForTenant(Long ruleId, Long tenantId) {
    IotAlarmRuleEntity rule = ruleMapper.findById(ruleId);
    if (rule == null || rule.getTenantId() == null || !rule.getTenantId().equals(tenantId)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "rule not found");
    }
    return rule;
  }

  private AlarmNotifyConfigResponse toConfigResponse(IotAlarmNotifyConfigEntity e, Map<Long, String> ruleNameById) {
    AlarmNotifyConfigResponse r = new AlarmNotifyConfigResponse();
    if (e == null) {
      return r;
    }
    r.setId(e.getId());
    r.setRuleId(e.getRuleId());
    String rn = ruleNameById.get(e.getRuleId());
    if (rn == null) {
      IotAlarmRuleEntity rule = ruleMapper.findById(e.getRuleId());
      rn = rule == null ? "" : rule.getName();
      if (rule != null) {
        ruleNameById.put(e.getRuleId(), rn);
      }
    }
    r.setRuleName(rn);
    r.setName(e.getName());
    r.setRemark(e.getRemark());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setEmailEnabled(e.getEmailEnabled() != null && e.getEmailEnabled() != 0);
    r.setSmsEnabled(e.getSmsEnabled() != null && e.getSmsEnabled() != 0);
    r.setEmailNotifyChannelId(e.getEmailNotifyChannelId());
    r.setSmsNotifyChannelId(e.getSmsNotifyChannelId());
    r.setEmailChannelName(channelName(e.getEmailNotifyChannelId()));
    r.setSmsChannelName(channelName(e.getSmsNotifyChannelId()));
    r.setToEmails(e.getToEmails());
    r.setToMobiles(e.getToMobiles());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private String channelName(Long id) {
    if (id == null) {
      return null;
    }
    IotNotifyChannelEntity ch = notifyChannelMapper.findById(id);
    return ch == null ? null : ch.getName();
  }

  private static AlarmNotifyDeliveryResponse toDeliveryResponse(IotAlarmNotifyDeliveryEntity row) {
    AlarmNotifyDeliveryResponse r = new AlarmNotifyDeliveryResponse();
    if (row == null) {
      return r;
    }
    r.setId(row.getId());
    r.setNotifyConfigId(row.getNotifyConfigId());
    r.setRuleId(row.getRuleId());
    r.setAlarmEventId(row.getAlarmEventId());
    r.setAlarmEventType(row.getAlarmEventType());
    r.setChannel(row.getChannel());
    r.setStatus(row.getStatus());
    r.setVendorCode(row.getVendorCode());
    r.setHttpStatus(row.getHttpStatus());
    r.setErrorMessage(row.getErrorMessage());
    r.setRecipientHint(row.getRecipientHint());
    r.setTest(row.getIsTest() != null && row.getIsTest() != 0);
    r.setCreatedAt(row.getCreatedAt());
    return r;
  }

  private static String trimOrNull(String s) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}

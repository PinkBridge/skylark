package cn.skylark.aiot_service.iot.alarm.service;

import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordPageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordPageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRecordResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleCreateRequest;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRulePageQuery;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRulePageResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleResponse;
import cn.skylark.aiot_service.iot.alarm.dto.AlarmRuleUpdateRequest;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmEntity;
import cn.skylark.aiot_service.iot.alarm.entity.IotAlarmRuleEntity;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmMapper;
import cn.skylark.aiot_service.iot.alarm.mapper.IotAlarmRuleMapper;
import cn.skylark.aiot_service.iot.alarm.support.AlarmRecordTriggerFormatter;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceMapper;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DeviceEntity;
import cn.skylark.aiot_service.iot.mgmt.service.AiotDataDomainSupport;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class AlarmMgmtService {
  private final IotAlarmRuleMapper ruleMapper;
  private final IotAlarmMapper alarmMapper;
  private final DeviceMapper deviceMapper;
  private final ObjectMapper objectMapper;

  public AlarmMgmtService(IotAlarmRuleMapper ruleMapper,
                          IotAlarmMapper alarmMapper,
                          DeviceMapper deviceMapper,
                          ObjectMapper objectMapper) {
    this.ruleMapper = ruleMapper;
    this.alarmMapper = alarmMapper;
    this.deviceMapper = deviceMapper;
    this.objectMapper = objectMapper;
  }

  public AlarmRulePageResponse listRules(AlarmRulePageQuery query) {
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    Integer enabled = query.getEnabled() == null ? null : (query.getEnabled() ? 1 : 0);
    String gk = trimToNull(query.getDeviceGroupKey());

    AlarmRulePageResponse resp = new AlarmRulePageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(ruleMapper.countPage(gk, enabled));
    List<IotAlarmRuleEntity> rows = ruleMapper.listPage(gk, enabled, offset, pageSize);
    List<AlarmRuleResponse> records = new ArrayList<AlarmRuleResponse>();
    if (rows != null) {
      for (IotAlarmRuleEntity e : rows) {
        records.add(toRuleResponse(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public AlarmRuleResponse createRule(AlarmRuleCreateRequest req) {
    validateRuleReq(req.getSourceType(), req.getSeverity(), req.getTriggerMode(), req.getRecoveryMode(), req.getDedupMode(),
        req.getDurationSeconds(), req.getConditionJson());
    IotAlarmRuleEntity e = new IotAlarmRuleEntity();
    e.setTenantId(AiotDataDomainSupport.requireTenantId());
    e.setOrgId(null);
    e.setDeviceGroupKey(req.getDeviceGroupKey().trim());
    e.setName(req.getName().trim());
    e.setSourceType(req.getSourceType().trim().toUpperCase());
    e.setSeverity(req.getSeverity().trim().toUpperCase());
    e.setTriggerMode(req.getTriggerMode().trim().toUpperCase());
    e.setDurationSeconds(normalizeDuration(req.getTriggerMode(), req.getDurationSeconds()));
    e.setRecoveryMode(req.getRecoveryMode().trim().toUpperCase());
    e.setDedupMode(req.getDedupMode().trim().toUpperCase());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConditionJson(req.getConditionJson());
    ruleMapper.insert(e);
    return toRuleResponse(ruleMapper.findById(e.getId()));
  }

  public AlarmRuleResponse getRule(Long id) {
    IotAlarmRuleEntity e = ruleMapper.findById(id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "rule not found");
    return toRuleResponse(e);
  }

  public AlarmRuleResponse updateRule(Long id, AlarmRuleUpdateRequest req) {
    validateRuleReq(req.getSourceType(), req.getSeverity(), req.getTriggerMode(), req.getRecoveryMode(), req.getDedupMode(),
        req.getDurationSeconds(), req.getConditionJson());
    IotAlarmRuleEntity e = ruleMapper.findById(id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "rule not found");
    e.setDeviceGroupKey(req.getDeviceGroupKey().trim());
    e.setName(req.getName().trim());
    e.setSourceType(req.getSourceType().trim().toUpperCase());
    e.setSeverity(req.getSeverity().trim().toUpperCase());
    e.setTriggerMode(req.getTriggerMode().trim().toUpperCase());
    e.setDurationSeconds(normalizeDuration(req.getTriggerMode(), req.getDurationSeconds()));
    e.setRecoveryMode(req.getRecoveryMode().trim().toUpperCase());
    e.setDedupMode(req.getDedupMode().trim().toUpperCase());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConditionJson(req.getConditionJson());
    if (ruleMapper.update(e) == 0) throw new MgmtException(HttpStatus.NOT_FOUND, "rule not found");
    return toRuleResponse(ruleMapper.findById(id));
  }

  public void deleteRule(Long id) {
    if (ruleMapper.softDeleteById(id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "rule not found");
    }
  }

  public AlarmRecordResponse recoverRecordManually(Long alarmId) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotAlarmEntity e = alarmMapper.findById(alarmId);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "alarm not found");
    }
    if (e.getTenantId() != null && !e.getTenantId().equals(tenantId)) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "alarm not found");
    }
    String st = normalizeEnum(e.getStatus());
    if (!"ACTIVE".equals(st)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "only ACTIVE alarm can be manually recovered");
    }
    IotAlarmRuleEntity rule = ruleMapper.findById(e.getRuleId());
    if (rule == null) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "rule not found");
    }
    if (rule.getTenantId() != null && !rule.getTenantId().equals(tenantId)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "rule not found");
    }
    String rm = normalizeEnum(rule.getRecoveryMode());
    if (!"MANUAL".equals(rm)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "manual recover only allowed when rule recoveryMode is MANUAL");
    }
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
    e.setStatus("RECOVERED");
    e.setRecoveredAt(now);
    if (alarmMapper.update(e) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "alarm not found");
    }
    return getRecord(alarmId);
  }

  public AlarmRecordPageResponse listRecords(AlarmRecordPageQuery query) {
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    AlarmRecordPageResponse resp = new AlarmRecordPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    String gk = trimToNull(query.getDeviceGroupKey());
    String severity = trimToNull(query.getSeverity());
    String status = trimToNull(query.getStatus());
    Long ruleId = query.getRuleId();
    resp.setTotal(alarmMapper.countPage(gk, ruleId, severity, status));
    List<IotAlarmEntity> rows = alarmMapper.listPage(gk, ruleId, severity, status, offset, pageSize);
    List<AlarmRecordResponse> records = new ArrayList<AlarmRecordResponse>();
    if (rows != null) {
      Map<Long, IotAlarmRuleEntity> ruleById = new HashMap<Long, IotAlarmRuleEntity>();
      Map<String, String> deviceNameByPkDk = new HashMap<String, String>();
      for (IotAlarmEntity e : rows) {
        records.add(toRecordResponse(e, ruleById, deviceNameByPkDk));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public AlarmRecordResponse getRecord(Long id) {
    IotAlarmEntity e = alarmMapper.findById(id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "alarm not found");
    return toRecordResponse(e, new HashMap<Long, IotAlarmRuleEntity>(), new HashMap<String, String>());
  }

  private AlarmRuleResponse toRuleResponse(IotAlarmRuleEntity e) {
    AlarmRuleResponse r = new AlarmRuleResponse();
    if (e == null) return r;
    r.setId(e.getId());
    r.setDeviceGroupKey(e.getDeviceGroupKey());
    r.setName(e.getName());
    r.setSourceType(e.getSourceType());
    r.setSeverity(e.getSeverity());
    r.setTriggerMode(e.getTriggerMode());
    r.setDurationSeconds(e.getDurationSeconds());
    r.setRecoveryMode(e.getRecoveryMode());
    r.setDedupMode(e.getDedupMode());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setConditionJson(e.getConditionJson());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private AlarmRecordResponse toRecordResponse(IotAlarmEntity e,
                                               Map<Long, IotAlarmRuleEntity> ruleById,
                                               Map<String, String> deviceNameByPkDk) {
    AlarmRecordResponse r = new AlarmRecordResponse();
    if (e == null) return r;
    r.setId(e.getId());
    r.setRuleId(e.getRuleId());
    r.setDeviceGroupKey(e.getDeviceGroupKey());
    r.setProductKey(e.getProductKey());
    r.setDeviceKey(e.getDeviceKey());
    r.setSeverity(e.getSeverity());
    r.setStatus(e.getStatus());
    r.setFirstTriggeredAt(e.getFirstTriggeredAt());
    r.setLastTriggeredAt(e.getLastTriggeredAt());
    r.setRecoveredAt(e.getRecoveredAt());
    r.setTriggerCount(e.getTriggerCount());
    r.setEvidenceJson(e.getEvidenceJson());
    r.setTriggerCondition(AlarmRecordTriggerFormatter.conditionSummary(objectMapper, e.getEvidenceJson()));
    r.setTriggerValue(AlarmRecordTriggerFormatter.valueDisplay(objectMapper, e.getEvidenceJson(),
        e.getLastEventType(), e.getLastEventId()));
    r.setLastEventId(e.getLastEventId());
    r.setLastEventType(e.getLastEventType());
    r.setCreatedAt(e.getCreatedAt());

    IotAlarmRuleEntity rule = resolveRule(e.getRuleId(), ruleById);
    if (rule != null && StringUtils.hasText(rule.getName())) {
      r.setRuleName(rule.getName().trim());
    } else {
      r.setRuleName("");
    }
    if (rule != null && StringUtils.hasText(rule.getRecoveryMode())) {
      r.setRuleRecoveryMode(rule.getRecoveryMode().trim().toUpperCase());
    } else {
      r.setRuleRecoveryMode("");
    }
    String deviceName = resolveDeviceName(e.getProductKey(), e.getDeviceKey(), deviceNameByPkDk);
    r.setDeviceName(deviceName);
    return r;
  }

  private IotAlarmRuleEntity resolveRule(Long ruleId, Map<Long, IotAlarmRuleEntity> ruleById) {
    if (ruleId == null) {
      return null;
    }
    if (ruleById.containsKey(ruleId)) {
      return ruleById.get(ruleId);
    }
    IotAlarmRuleEntity rule = ruleMapper.findById(ruleId);
    ruleById.put(ruleId, rule);
    return rule;
  }

  private static String deviceCacheKey(String productKey, String deviceKey) {
    return (productKey == null ? "" : productKey) + "\0" + (deviceKey == null ? "" : deviceKey);
  }

  private String resolveDeviceName(String productKey, String deviceKey, Map<String, String> deviceNameByPkDk) {
    if (!StringUtils.hasText(productKey) || !StringUtils.hasText(deviceKey)) {
      return "";
    }
    String ck = deviceCacheKey(productKey, deviceKey);
    if (deviceNameByPkDk.containsKey(ck)) {
      return deviceNameByPkDk.get(ck);
    }
    DeviceEntity d = deviceMapper.findByPkAndDeviceKey(productKey.trim(), deviceKey.trim());
    String name = d != null && StringUtils.hasText(d.getDeviceName()) ? d.getDeviceName().trim() : "";
    deviceNameByPkDk.put(ck, name);
    return name;
  }

  private static int normalizeDuration(String triggerMode, Integer durationSeconds) {
    String tm = triggerMode == null ? "" : triggerMode.trim().toUpperCase();
    int d = durationSeconds == null ? 0 : durationSeconds;
    if ("DURATION".equals(tm)) {
      return Math.max(d, 1);
    }
    return 0;
  }

  private void validateRuleReq(String sourceType, String severity, String triggerMode,
                               String recoveryMode, String dedupMode, Integer durationSeconds, String conditionJson) {
    String st = normalizeEnum(sourceType);
    if (!"PROPERTY".equals(st) && !"EVENT".equals(st)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "sourceType invalid");
    }
    String sv = normalizeEnum(severity);
    if (!"HIGH".equals(sv) && !"MEDIUM".equals(sv) && !"LOW".equals(sv)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "severity invalid");
    }
    String tm = normalizeEnum(triggerMode);
    if (!"INSTANT".equals(tm) && !"DURATION".equals(tm)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "triggerMode invalid");
    }
    String rm = normalizeEnum(recoveryMode);
    if (!"AUTO".equals(rm) && !"MANUAL".equals(rm)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "recoveryMode invalid");
    }
    String dm = normalizeEnum(dedupMode);
    if (!"SINGLE_ACTIVE".equals(dm) && !"EVERY_TRIGGER".equals(dm)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "dedupMode invalid");
    }
    normalizeDuration(tm, durationSeconds);
    if (!StringUtils.hasText(conditionJson)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "conditionJson required");
    }
  }

  private static String normalizeEnum(String v) {
    if (!StringUtils.hasText(v)) return "";
    return v.trim().toUpperCase();
  }

  private static String trimToNull(String s) {
    if (!StringUtils.hasText(s)) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}


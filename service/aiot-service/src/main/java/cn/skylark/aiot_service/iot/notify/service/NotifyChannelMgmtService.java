package cn.skylark.aiot_service.iot.notify.service;

import cn.skylark.aiot_service.iot.mgmt.service.AiotDataDomainSupport;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelCreateRequest;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelPageQuery;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelPageResponse;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelResponse;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelTestRequest;
import cn.skylark.aiot_service.iot.notify.dto.NotifyChannelUpdateRequest;
import cn.skylark.aiot_service.iot.notify.entity.IotNotifyChannelEntity;
import cn.skylark.aiot_service.iot.notify.mapper.IotNotifyChannelMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotifyChannelMgmtService {
  private final IotNotifyChannelMapper channelMapper;
  private final NotifyChannelSender notifyChannelSender;
  private final ObjectMapper objectMapper;

  public NotifyChannelMgmtService(IotNotifyChannelMapper channelMapper,
                                  NotifyChannelSender notifyChannelSender,
                                  ObjectMapper objectMapper) {
    this.channelMapper = channelMapper;
    this.notifyChannelSender = notifyChannelSender;
    this.objectMapper = objectMapper;
  }

  public NotifyChannelPageResponse listChannels(NotifyChannelPageQuery query) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    String kind = trimToNull(query.getChannelKind());
    NotifyChannelPageResponse resp = new NotifyChannelPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(channelMapper.countPage(tenantId, kind));
    List<IotNotifyChannelEntity> rows = channelMapper.listPage(tenantId, kind, offset, pageSize);
    List<NotifyChannelResponse> records = new ArrayList<NotifyChannelResponse>();
    if (rows != null) {
      for (IotNotifyChannelEntity e : rows) {
        records.add(toResponseMasked(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public List<NotifyChannelResponse> listChannelsSimple(String channelKind) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    String kind = trimToNull(channelKind);
    if (!StringUtils.hasText(kind)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "channelKind required");
    }
    validateKind(kind);
    List<IotNotifyChannelEntity> rows = channelMapper.listByTenantAndKind(tenantId, kind.toUpperCase(), 1);
    List<NotifyChannelResponse> out = new ArrayList<NotifyChannelResponse>();
    if (rows != null) {
      for (IotNotifyChannelEntity e : rows) {
        out.add(toResponseMasked(e));
      }
    }
    return out;
  }

  public NotifyChannelResponse getChannel(Long id) {
    return toResponseMasked(requireChannel(id));
  }

  public NotifyChannelResponse createChannel(NotifyChannelCreateRequest req) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    String kind = req.getChannelKind().trim().toUpperCase();
    String provider = req.getProvider().trim().toUpperCase();
    validateKindProvider(kind, provider);
    validateConfigShape(kind, provider, req.getConfigJson());
    IotNotifyChannelEntity e = new IotNotifyChannelEntity();
    e.setTenantId(tenantId);
    e.setOrgId(AiotDataDomainSupport.currentOrgId());
    e.setChannelKind(kind);
    e.setProvider(provider);
    e.setName(req.getName().trim());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConfigJson(req.getConfigJson().trim());
    channelMapper.insert(e);
    return getChannel(e.getId());
  }

  public NotifyChannelResponse updateChannel(Long id, NotifyChannelUpdateRequest req) {
    IotNotifyChannelEntity e = requireChannel(id);
    String merged = NotifyChannelSender.mergeConfigJson(e.getConfigJson(), req.getConfigJson().trim(), objectMapper);
    validateConfigShape(e.getChannelKind(), e.getProvider(), merged);
    e.setName(req.getName().trim());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConfigJson(merged);
    if (channelMapper.update(e) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    return getChannel(id);
  }

  public void deleteChannel(Long id) {
    requireChannel(id);
    if (channelMapper.softDeleteById(id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
  }

  public void testChannel(Long id, NotifyChannelTestRequest req) {
    IotNotifyChannelEntity ch = requireChannel(id);
    if (ch.getEnabled() == null || ch.getEnabled() == 0) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "channel is disabled");
    }
    String kind = ch.getChannelKind() == null ? "" : ch.getChannelKind().toUpperCase();
    if ("EMAIL".equals(kind)) {
      if (!StringUtils.hasText(req.getTestEmail())) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "testEmail required");
      }
      notifyChannelSender.sendTestEmail(ch, req.getTestEmail().trim());
      return;
    }
    if ("SMS".equals(kind)) {
      if (!StringUtils.hasText(req.getTestPhone())) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "testPhone required");
      }
      notifyChannelSender.sendTestSmsFromChannel(ch, req.getTestPhone().trim());
      return;
    }
    throw new MgmtException(HttpStatus.BAD_REQUEST, "unsupported channel kind");
  }

  /** Raw entity for alarm dispatch (includes secrets). */
  public IotNotifyChannelEntity requireChannelForTenant(Long id, Long tenantId) {
    IotNotifyChannelEntity e = channelMapper.findById(id);
    if (e == null || e.getTenantId() == null || !e.getTenantId().equals(tenantId)) {
      return null;
    }
    return e;
  }

  private IotNotifyChannelEntity requireChannel(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotNotifyChannelEntity e = channelMapper.findById(id);
    if (e == null || e.getTenantId() == null || !e.getTenantId().equals(tenantId)) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    return e;
  }

  private NotifyChannelResponse toResponseMasked(IotNotifyChannelEntity e) {
    NotifyChannelResponse r = new NotifyChannelResponse();
    if (e == null) {
      return r;
    }
    r.setId(e.getId());
    r.setChannelKind(e.getChannelKind());
    r.setProvider(e.getProvider());
    r.setName(e.getName());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setConfigJson(NotifyChannelSender.maskSecretsInConfigJson(e.getConfigJson(), objectMapper));
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private static void validateKind(String kind) {
    if (!"EMAIL".equals(kind) && !"SMS".equals(kind)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "channelKind must be EMAIL or SMS");
    }
  }

  private static void validateKindProvider(String kind, String provider) {
    validateKind(kind);
    if ("EMAIL".equals(kind)) {
      if (!"SMTP".equals(provider)) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "EMAIL channels must use provider SMTP");
      }
      return;
    }
    if ("SMS".equals(kind)) {
      if (!"ALIYUN".equals(provider) && !"TWILIO".equals(provider) && !"AWS_SNS".equals(provider)) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "SMS provider must be ALIYUN, TWILIO, or AWS_SNS");
      }
    }
  }

  private void validateConfigShape(String kind, String provider, String configJson) {
    if (!StringUtils.hasText(configJson)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "configJson required");
    }
    try {
      objectMapper.readTree(configJson);
    } catch (Exception e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "configJson must be valid JSON");
    }
    if ("EMAIL".equalsIgnoreCase(kind) && "SMTP".equalsIgnoreCase(provider)) {
      return;
    }
    if ("SMS".equalsIgnoreCase(kind) && "ALIYUN".equalsIgnoreCase(provider)) {
      return;
    }
    if ("SMS".equalsIgnoreCase(kind) && "TWILIO".equalsIgnoreCase(provider)) {
      return;
    }
    if ("SMS".equalsIgnoreCase(kind) && "AWS_SNS".equalsIgnoreCase(provider)) {
      return;
    }
    throw new MgmtException(HttpStatus.BAD_REQUEST, "invalid kind/provider combination");
  }

  private static String trimToNull(String s) {
    if (!StringUtils.hasText(s)) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t.toUpperCase();
  }
}

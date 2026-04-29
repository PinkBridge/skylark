package cn.skylark.aiot_service.iot.appint.service;

import cn.skylark.aiot_service.iot.appint.ConfigSecretMasker;
import cn.skylark.aiot_service.iot.appint.OutboundDispatchService;
import cn.skylark.aiot_service.iot.appint.WebhookOutboundClient;
import cn.skylark.aiot_service.iot.appint.model.IotIntegrationEventType;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundChannelMapper;
import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundDeliveryMapper;
import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundSubscriptionMapper;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelCreateRequest;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelPageQuery;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelPageResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelUpdateRequest;
import cn.skylark.aiot_service.iot.appint.dto.OutboundDeliveryPageQuery;
import cn.skylark.aiot_service.iot.appint.dto.OutboundDeliveryPageResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundDeliveryResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionCreateRequest;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionPageQuery;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionPageResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionUpdateRequest;
import cn.skylark.aiot_service.iot.appint.dto.TestWebhookResponse;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundChannelEntity;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundDeliveryEntity;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundSubscriptionEntity;
import cn.skylark.aiot_service.iot.mgmt.service.AiotDataDomainSupport;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IntegrationOutboundMgmtService {
  private final IotOutboundChannelMapper channelMapper;
  private final IotOutboundSubscriptionMapper subscriptionMapper;
  private final IotOutboundDeliveryMapper deliveryMapper;
  private final OutboundDispatchService outboundDispatchService;
  private final ObjectMapper objectMapper;

  public IntegrationOutboundMgmtService(IotOutboundChannelMapper channelMapper,
                                        IotOutboundSubscriptionMapper subscriptionMapper,
                                        IotOutboundDeliveryMapper deliveryMapper,
                                        OutboundDispatchService outboundDispatchService,
                                        ObjectMapper objectMapper) {
    this.channelMapper = channelMapper;
    this.subscriptionMapper = subscriptionMapper;
    this.deliveryMapper = deliveryMapper;
    this.outboundDispatchService = outboundDispatchService;
    this.objectMapper = objectMapper;
  }

  public List<String> listEventTypes() {
    List<String> list = new ArrayList<String>();
    Collections.addAll(list, IotIntegrationEventType.all());
    return list;
  }

  public OutboundChannelPageResponse listChannels(OutboundChannelPageQuery query) {
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    OutboundChannelPageResponse resp = new OutboundChannelPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(channelMapper.countAll());
    List<IotOutboundChannelEntity> rows = channelMapper.listPage(offset, pageSize);
    List<OutboundChannelResponse> records = new ArrayList<OutboundChannelResponse>();
    if (rows != null) {
      for (IotOutboundChannelEntity e : rows) {
        records.add(toChannelResponse(e, true));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public OutboundChannelResponse createChannel(OutboundChannelCreateRequest req) {
    IotOutboundChannelEntity e = new IotOutboundChannelEntity();
    e.setTenantId(AiotDataDomainSupport.requireTenantId());
    e.setOrgId(AiotDataDomainSupport.currentOrgId());
    e.setName(req.getName().trim());
    e.setType(req.getType().trim().toUpperCase());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConfigJson(req.getConfigJson());
    channelMapper.insert(e);
    IotOutboundChannelEntity loaded = channelMapper.findById(e.getId());
    return toChannelResponse(loaded, true);
  }

  public OutboundChannelResponse getChannel(Long id) {
    IotOutboundChannelEntity e = channelMapper.findById(id);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    return toChannelResponse(e, true);
  }

  public OutboundChannelResponse updateChannel(Long id, OutboundChannelUpdateRequest req) {
    IotOutboundChannelEntity e = channelMapper.findById(id);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    e.setName(req.getName().trim());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConfigJson(req.getConfigJson());
    if (channelMapper.update(e) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    return toChannelResponse(channelMapper.findById(id), true);
  }

  public void deleteChannel(Long id) {
    if (channelMapper.softDeleteById(id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
  }

  public TestWebhookResponse testChannel(Long id) {
    IotOutboundChannelEntity e = channelMapper.findById(id);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    NormalizedEvent sample = sampleEvent(AiotDataDomainSupport.requireTenantId(), AiotDataDomainSupport.currentOrgId());
    WebhookOutboundClient.WebhookSendResult r =
        outboundDispatchService.dispatchTestWebhook(e.getId(), e.getType(), e.getConfigJson(), sample);
    TestWebhookResponse resp = new TestWebhookResponse();
    resp.setOk(r.isOk());
    resp.setHttpStatus(r.getHttpStatus() > 0 ? r.getHttpStatus() : null);
    resp.setError(r.getError());
    return resp;
  }

  public OutboundSubscriptionPageResponse listSubscriptions(OutboundSubscriptionPageQuery query) {
    if (query.getChannelId() == null) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "channelId required");
    }
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    OutboundSubscriptionPageResponse resp = new OutboundSubscriptionPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(subscriptionMapper.countByChannel(query.getChannelId()));
    List<IotOutboundSubscriptionEntity> rows = subscriptionMapper.listByChannel(query.getChannelId(), offset, pageSize);
    List<OutboundSubscriptionResponse> records = new ArrayList<OutboundSubscriptionResponse>();
    if (rows != null) {
      for (IotOutboundSubscriptionEntity e : rows) {
        records.add(toSubscriptionResponse(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public OutboundSubscriptionResponse createSubscription(OutboundSubscriptionCreateRequest req) {
    IotOutboundChannelEntity ch = channelMapper.findById(req.getChannelId());
    if (ch == null) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "channel not found");
    }
    IotOutboundSubscriptionEntity e = new IotOutboundSubscriptionEntity();
    e.setTenantId(AiotDataDomainSupport.requireTenantId());
    e.setOrgId(AiotDataDomainSupport.currentOrgId());
    e.setChannelId(req.getChannelId());
    e.setName(req.getName() == null ? null : req.getName().trim());
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setEventTypes(writeEventTypes(req.getEventTypes()));
    e.setFilterJson(trimToNull(req.getFilterJson()));
    e.setTransformJson(trimToNull(req.getTransformJson()));
    subscriptionMapper.insert(e);
    return toSubscriptionResponse(subscriptionMapper.findById(e.getId()));
  }

  public OutboundSubscriptionResponse getSubscription(Long id) {
    IotOutboundSubscriptionEntity e = subscriptionMapper.findById(id);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    }
    return toSubscriptionResponse(e);
  }

  public OutboundSubscriptionResponse updateSubscription(Long id, OutboundSubscriptionUpdateRequest req) {
    IotOutboundSubscriptionEntity e = subscriptionMapper.findById(id);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    }
    if (req.getName() != null) {
      e.setName(req.getName().trim());
    }
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setEventTypes(writeEventTypes(req.getEventTypes()));
    e.setFilterJson(trimToNull(req.getFilterJson()));
    e.setTransformJson(trimToNull(req.getTransformJson()));
    if (subscriptionMapper.update(e) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    }
    return toSubscriptionResponse(subscriptionMapper.findById(id));
  }

  public void deleteSubscription(Long id) {
    if (subscriptionMapper.softDeleteById(id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    }
  }

  public OutboundDeliveryPageResponse listDeliveries(OutboundDeliveryPageQuery query) {
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    OutboundDeliveryPageResponse resp = new OutboundDeliveryPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(deliveryMapper.countAll());
    List<IotOutboundDeliveryEntity> rows = deliveryMapper.listPage(offset, pageSize);
    List<OutboundDeliveryResponse> records = new ArrayList<OutboundDeliveryResponse>();
    if (rows != null) {
      for (IotOutboundDeliveryEntity e : rows) {
        records.add(toDeliveryResponse(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public void retryDelivery(Long id) {
    IotOutboundDeliveryEntity d = deliveryMapper.findById(id);
    if (d == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "delivery not found");
    }
    if ("success".equalsIgnoreCase(d.getStatus())) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "delivery already success");
    }
    if ("dead".equalsIgnoreCase(d.getStatus())) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "delivery is dead");
    }
    d.setStatus("failed");
    d.setNextRetryAt(LocalDateTime.now(ZoneId.systemDefault()));
    deliveryMapper.update(d);
  }

  private OutboundChannelResponse toChannelResponse(IotOutboundChannelEntity e, boolean maskSecrets) {
    OutboundChannelResponse r = new OutboundChannelResponse();
    if (e == null) {
      return r;
    }
    r.setId(e.getId());
    r.setName(e.getName());
    r.setType(e.getType());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setConfigJson(maskSecrets ? ConfigSecretMasker.maskJson(e.getConfigJson(), objectMapper) : e.getConfigJson());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private OutboundSubscriptionResponse toSubscriptionResponse(IotOutboundSubscriptionEntity e) {
    OutboundSubscriptionResponse r = new OutboundSubscriptionResponse();
    if (e == null) {
      return r;
    }
    r.setId(e.getId());
    r.setChannelId(e.getChannelId());
    r.setName(e.getName());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setEventTypes(readEventTypes(e.getEventTypes()));
    r.setFilterJson(e.getFilterJson());
    r.setTransformJson(e.getTransformJson());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private OutboundDeliveryResponse toDeliveryResponse(IotOutboundDeliveryEntity e) {
    OutboundDeliveryResponse r = new OutboundDeliveryResponse();
    if (e == null) {
      return r;
    }
    r.setId(e.getId());
    r.setEventId(e.getEventId());
    r.setEventType(e.getEventType());
    r.setSubscriptionId(e.getSubscriptionId());
    r.setChannelId(e.getChannelId());
    r.setStatus(e.getStatus());
    r.setAttempts(e.getAttempts());
    r.setNextRetryAt(e.getNextRetryAt());
    r.setLastError(e.getLastError());
    r.setHttpStatus(e.getHttpStatus());
    r.setPayloadSnapshot(e.getPayloadSnapshot());
    r.setCreatedAt(e.getCreatedAt());
    return r;
  }

  private String writeEventTypes(List<String> types) {
    try {
      return objectMapper.writeValueAsString(types);
    } catch (JsonProcessingException e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "eventTypes invalid");
    }
  }

  private List<String> readEventTypes(String json) {
    if (!StringUtils.hasText(json)) {
      return Collections.emptyList();
    }
    try {
      return objectMapper.readValue(json,
          objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  private static String trimToNull(String s) {
    if (!StringUtils.hasText(s)) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  private static NormalizedEvent sampleEvent(Long tenantId, Long orgId) {
    Map<String, String> subject = new LinkedHashMap<String, String>();
    subject.put("productKey", "demoProduct");
    subject.put("deviceKey", "demoDevice");
    Map<String, Object> data = new LinkedHashMap<String, Object>();
    data.put("message", "skylark integration test");
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
}

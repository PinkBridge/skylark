package cn.skylark.aiot_service.iot.notification.service;

import cn.skylark.aiot_service.iot.appint.model.IotIntegrationEventType;
import cn.skylark.aiot_service.iot.mgmt.service.AiotDataDomainSupport;
import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.aiot_service.iot.notification.dto.NotificationChannelCreateRequest;
import cn.skylark.aiot_service.iot.notification.dto.NotificationChannelPageQuery;
import cn.skylark.aiot_service.iot.notification.dto.NotificationChannelPageResponse;
import cn.skylark.aiot_service.iot.notification.dto.NotificationChannelResponse;
import cn.skylark.aiot_service.iot.notification.dto.NotificationChannelUpdateRequest;
import cn.skylark.aiot_service.iot.notification.dto.NotificationDeliveryPageQuery;
import cn.skylark.aiot_service.iot.notification.dto.NotificationDeliveryPageResponse;
import cn.skylark.aiot_service.iot.notification.dto.NotificationDeliveryResponse;
import cn.skylark.aiot_service.iot.notification.dto.NotificationSubscriptionCreateRequest;
import cn.skylark.aiot_service.iot.notification.dto.NotificationSubscriptionPageQuery;
import cn.skylark.aiot_service.iot.notification.dto.NotificationSubscriptionPageResponse;
import cn.skylark.aiot_service.iot.notification.dto.NotificationSubscriptionResponse;
import cn.skylark.aiot_service.iot.notification.dto.NotificationSubscriptionUpdateRequest;
import cn.skylark.aiot_service.iot.notification.dto.NotificationTestResult;
import cn.skylark.aiot_service.iot.notification.entity.IotNotificationChannelEntity;
import cn.skylark.aiot_service.iot.notification.entity.IotNotificationDeliveryEntity;
import cn.skylark.aiot_service.iot.notification.entity.IotNotificationSubscriptionEntity;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationChannelMapper;
import cn.skylark.aiot_service.iot.notification.service.NotificationDispatchService;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationDeliveryMapper;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationSubscriptionMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationMgmtService {
  private final IotNotificationChannelMapper channelMapper;
  private final IotNotificationSubscriptionMapper subscriptionMapper;
  private final IotNotificationDeliveryMapper deliveryMapper;
  private final NotificationDispatchService notificationDispatchService;
  private final ObjectMapper objectMapper;

  public NotificationMgmtService(IotNotificationChannelMapper channelMapper,
                                 IotNotificationSubscriptionMapper subscriptionMapper,
                                 IotNotificationDeliveryMapper deliveryMapper,
                                 NotificationDispatchService notificationDispatchService,
                                 ObjectMapper objectMapper) {
    this.channelMapper = channelMapper;
    this.subscriptionMapper = subscriptionMapper;
    this.deliveryMapper = deliveryMapper;
    this.notificationDispatchService = notificationDispatchService;
    this.objectMapper = objectMapper;
  }

  public List<String> listEventTypes() {
    List<String> list = new ArrayList<String>();
    Collections.addAll(list, IotIntegrationEventType.all());
    list.add("alarm.triggered");
    list.add("alarm.recovered");
    return list;
  }

  public NotificationChannelPageResponse listChannels(NotificationChannelPageQuery query) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;

    NotificationChannelPageResponse resp = new NotificationChannelPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(channelMapper.countAll(tenantId));

    List<IotNotificationChannelEntity> rows = channelMapper.listPage(tenantId, offset, pageSize);
    List<NotificationChannelResponse> records = new ArrayList<NotificationChannelResponse>();
    if (rows != null) {
      for (IotNotificationChannelEntity e : rows) {
        records.add(toChannelResponse(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public NotificationChannelResponse createChannel(NotificationChannelCreateRequest req) {
    validateJson(req.getConfigJson(), "configJson");
    IotNotificationChannelEntity e = new IotNotificationChannelEntity();
    e.setTenantId(AiotDataDomainSupport.requireTenantId());
    e.setOrgId(null);
    e.setName(req.getName().trim());
    e.setType(normalizeChannelType(req.getType()));
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConfigJson(req.getConfigJson());
    channelMapper.insert(e);
    IotNotificationChannelEntity loaded = channelMapper.findById(e.getTenantId(), e.getId());
    return toChannelResponse(loaded);
  }

  public NotificationChannelResponse getChannel(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotNotificationChannelEntity e = channelMapper.findById(tenantId, id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    return toChannelResponse(e);
  }

  public NotificationChannelResponse updateChannel(Long id, NotificationChannelUpdateRequest req) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    validateJson(req.getConfigJson(), "configJson");
    IotNotificationChannelEntity e = channelMapper.findById(tenantId, id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    e.setName(req.getName().trim());
    e.setType(normalizeChannelType(req.getType()));
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setConfigJson(req.getConfigJson());
    channelMapper.update(e);
    return toChannelResponse(channelMapper.findById(tenantId, id));
  }

  public void deleteChannel(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    if (channelMapper.softDeleteById(tenantId, id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
  }

  public NotificationSubscriptionPageResponse listSubscriptions(NotificationSubscriptionPageQuery query) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    String gk = trimToNull(query.getDeviceGroupKey());

    NotificationSubscriptionPageResponse resp = new NotificationSubscriptionPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(subscriptionMapper.countPage(tenantId, query.getChannelId(), gk));

    List<IotNotificationSubscriptionEntity> rows = subscriptionMapper.listPage(tenantId, query.getChannelId(), gk, offset, pageSize);
    List<NotificationSubscriptionResponse> records = new ArrayList<NotificationSubscriptionResponse>();
    if (rows != null) {
      for (IotNotificationSubscriptionEntity e : rows) {
        records.add(toSubscriptionResponse(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public NotificationSubscriptionResponse createSubscription(NotificationSubscriptionCreateRequest req) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    if (req.getEventTypes() == null || req.getEventTypes().isEmpty()) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "eventTypes required");
    }
    validateJson(req.getFilterJson(), "filterJson");
    validateTemplateJson(req.getTemplateJson());
    IotNotificationSubscriptionEntity e = new IotNotificationSubscriptionEntity();
    e.setTenantId(tenantId);
    e.setOrgId(null);
    e.setChannelId(req.getChannelId());
    e.setName(trimToNull(req.getName()));
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setDeviceGroupKey(req.getDeviceGroupKey().trim());
    e.setEventTypes(writeJson(req.getEventTypes()));
    e.setFilterJson(trimToNull(req.getFilterJson()));
    e.setTemplateJson(req.getTemplateJson());
    subscriptionMapper.insert(e);
    return toSubscriptionResponse(subscriptionMapper.findById(tenantId, e.getId()));
  }

  public NotificationSubscriptionResponse getSubscription(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotNotificationSubscriptionEntity e = subscriptionMapper.findById(tenantId, id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    return toSubscriptionResponse(e);
  }

  public NotificationSubscriptionResponse updateSubscription(Long id, NotificationSubscriptionUpdateRequest req) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    if (req.getEventTypes() == null || req.getEventTypes().isEmpty()) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "eventTypes required");
    }
    validateJson(req.getFilterJson(), "filterJson");
    validateTemplateJson(req.getTemplateJson());
    IotNotificationSubscriptionEntity e = subscriptionMapper.findById(tenantId, id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    e.setChannelId(req.getChannelId());
    e.setName(trimToNull(req.getName()));
    e.setEnabled(req.getEnabled() ? 1 : 0);
    e.setDeviceGroupKey(req.getDeviceGroupKey().trim());
    e.setEventTypes(writeJson(req.getEventTypes()));
    e.setFilterJson(trimToNull(req.getFilterJson()));
    e.setTemplateJson(req.getTemplateJson());
    subscriptionMapper.update(e);
    return toSubscriptionResponse(subscriptionMapper.findById(tenantId, id));
  }

  public void deleteSubscription(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    if (subscriptionMapper.softDeleteById(tenantId, id) == 0) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "subscription not found");
    }
  }

  public NotificationDeliveryPageResponse listDeliveries(NotificationDeliveryPageQuery query) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
    int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : Math.min(query.getPageSize(), 100);
    int offset = (pageNum - 1) * pageSize;
    String status = trimToNull(query.getStatus());

    NotificationDeliveryPageResponse resp = new NotificationDeliveryPageResponse();
    resp.setPageNum(pageNum);
    resp.setPageSize(pageSize);
    resp.setTotal(deliveryMapper.countPage(tenantId, status, query.getChannelId(), query.getSubscriptionId()));

    List<IotNotificationDeliveryEntity> rows = deliveryMapper.listPage(tenantId, status, query.getChannelId(), query.getSubscriptionId(), offset, pageSize);
    List<NotificationDeliveryResponse> records = new ArrayList<NotificationDeliveryResponse>();
    if (rows != null) {
      for (IotNotificationDeliveryEntity e : rows) {
        records.add(toDeliveryResponse(e));
      }
    }
    resp.setRecords(records);
    return resp;
  }

  public NotificationDeliveryResponse getDelivery(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotNotificationDeliveryEntity e = deliveryMapper.findById(tenantId, id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "delivery not found");
    return toDeliveryResponse(e);
  }

  public void retryDelivery(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotNotificationDeliveryEntity e = deliveryMapper.findById(tenantId, id);
    if (e == null) throw new MgmtException(HttpStatus.NOT_FOUND, "delivery not found");
    if ("success".equalsIgnoreCase(e.getStatus())) {
      return;
    }
    e.setStatus("failed");
    e.setNextRetryAt(java.time.LocalDateTime.now());
    deliveryMapper.update(e);
    IotNotificationDeliveryEntity fresh = deliveryMapper.findById(tenantId, id);
    if (fresh != null) {
      notificationDispatchService.resendDelivery(fresh);
    }
  }

  public NotificationTestResult testChannel(Long id) {
    Long tenantId = AiotDataDomainSupport.requireTenantId();
    IotNotificationChannelEntity e = channelMapper.findById(tenantId, id);
    if (e == null) {
      throw new MgmtException(HttpStatus.NOT_FOUND, "channel not found");
    }
    if (!StringUtils.hasText(e.getConfigJson())) {
      return NotificationTestResult.fail("configJson empty");
    }
    return notificationDispatchService.testChannel(tenantId, e);
  }

  private NotificationChannelResponse toChannelResponse(IotNotificationChannelEntity e) {
    NotificationChannelResponse r = new NotificationChannelResponse();
    if (e == null) return r;
    r.setId(e.getId());
    r.setName(e.getName());
    r.setType(e.getType());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setConfigJson(e.getConfigJson());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private NotificationSubscriptionResponse toSubscriptionResponse(IotNotificationSubscriptionEntity e) {
    NotificationSubscriptionResponse r = new NotificationSubscriptionResponse();
    if (e == null) return r;
    r.setId(e.getId());
    r.setChannelId(e.getChannelId());
    r.setName(e.getName());
    r.setEnabled(e.getEnabled() != null && e.getEnabled() != 0);
    r.setDeviceGroupKey(e.getDeviceGroupKey());
    r.setEventTypes(parseStringList(e.getEventTypes()));
    r.setFilterJson(e.getFilterJson());
    r.setTemplateJson(e.getTemplateJson());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  private NotificationDeliveryResponse toDeliveryResponse(IotNotificationDeliveryEntity e) {
    NotificationDeliveryResponse r = new NotificationDeliveryResponse();
    if (e == null) return r;
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
    r.setRenderedTitle(e.getRenderedTitle());
    r.setRenderedBody(e.getRenderedBody());
    r.setCreatedAt(e.getCreatedAt());
    return r;
  }

  private String normalizeChannelType(String type) {
    if (!StringUtils.hasText(type)) return "";
    return type.trim().toUpperCase();
  }

  private void validateJson(String maybeJson, String field) {
    if (!StringUtils.hasText(maybeJson)) {
      return;
    }
    try {
      objectMapper.readTree(maybeJson);
    } catch (Exception e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, field + " invalid json");
    }
  }

  private void validateTemplateJson(String templateJson) {
    if (!StringUtils.hasText(templateJson)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "templateJson required");
    }
    try {
      JsonNode n = objectMapper.readTree(templateJson);
      String title = n.path("title").asText("");
      String body = n.path("body").asText("");
      if (!StringUtils.hasText(title) && !StringUtils.hasText(body)) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "templateJson.title/body required");
      }
    } catch (MgmtException e) {
      throw e;
    } catch (Exception e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "templateJson invalid json");
    }
  }

  private String writeJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "json write failed");
    }
  }

  private List<String> parseStringList(String json) {
    if (!StringUtils.hasText(json)) return Collections.emptyList();
    try {
      String[] arr = objectMapper.readValue(json, String[].class);
      return arr == null ? Collections.emptyList() : Arrays.asList(arr);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  private static String trimToNull(String s) {
    if (!StringUtils.hasText(s)) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}


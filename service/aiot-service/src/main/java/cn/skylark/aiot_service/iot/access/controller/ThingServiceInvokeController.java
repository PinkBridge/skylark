package cn.skylark.aiot_service.iot.access.controller;

import cn.skylark.aiot_service.iot.access.listener.ThingServiceReplyListener;
import cn.skylark.aiot_service.iot.access.model.DownstreamPublishRequest;
import cn.skylark.aiot_service.iot.access.model.InvokeThingServiceRequest;
import cn.skylark.aiot_service.iot.access.model.InvokeThingServiceResponse;
import cn.skylark.aiot_service.iot.access.service.EmqxManagementClient;
import cn.skylark.aiot_service.iot.access.service.ThingServiceReplyAwaiter;
import cn.skylark.aiot_service.iot.mgmt.mapper.DeviceRecordMapper;
import cn.skylark.aiot_service.iot.mgmt.mapper.ProductMapper;
import cn.skylark.aiot_service.iot.mgmt.model.entity.DeviceServiceRecordEntity;
import cn.skylark.aiot_service.iot.mgmt.model.entity.ProductEntity;
import cn.skylark.datadomain.starter.context.DataDomainContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/aiot-service/access")
public class ThingServiceInvokeController {
  private final EmqxManagementClient emqxManagementClient;
  private final ThingServiceReplyAwaiter replyAwaiter;
  private final DeviceRecordMapper deviceRecordMapper;
  private final ProductMapper productMapper;
  private final ObjectMapper objectMapper;

  public ThingServiceInvokeController(EmqxManagementClient emqxManagementClient,
                                      ThingServiceReplyAwaiter replyAwaiter,
                                      DeviceRecordMapper deviceRecordMapper,
                                      ProductMapper productMapper,
                                      ObjectMapper objectMapper) {
    this.emqxManagementClient = emqxManagementClient;
    this.replyAwaiter = replyAwaiter;
    this.deviceRecordMapper = deviceRecordMapper;
    this.productMapper = productMapper;
    this.objectMapper = objectMapper;
  }

  @PostMapping({"/invoke-thing-service", "/invokeThingService"})
  public ResponseEntity<InvokeThingServiceResponse> invoke(@Validated @RequestBody InvokeThingServiceRequest request) {
    String requestId = newId();
    String messageId = newId();
    String productKey = safe(request.getProductKey());
    String deviceName = safe(request.getDeviceName());
    String identifier = safe(request.getIdentifier());
    int qos = request.getQos() == null ? 1 : request.getQos();
    boolean sync = Boolean.TRUE.equals(request.getSync());
    int timeoutMs = normalizeTimeout(request.getTimeoutMs());
    String key = ThingServiceReplyListener.buildKey(productKey, deviceName, identifier, messageId);
    CompletableFuture<String> pending = null;
    if (sync) {
      // Register before publish to avoid missing ultra-fast replies.
      pending = replyAwaiter.register(key);
    }

    DownstreamPublishRequest publishRequest = new DownstreamPublishRequest();
    publishRequest.setTraceId(requestId);
    publishRequest.setTopic("/sys/" + productKey + "/" + deviceName + "/thing/service/" + identifier + "/invoke");
    try {
      publishRequest.setPayload(buildPayload(messageId, identifier, request.getArgs()));
    } catch (IllegalArgumentException ex) {
      if (sync) {
        replyAwaiter.clear(key);
      }
      return ResponseEntity.ok(fail(requestId, ex.getMessage()));
    }
    publishRequest.setQos(qos);
    publishRequest.setRetain(false);
    persistInvokeRecord(productKey, deviceName, identifier, requestId, messageId, publishRequest);

    Optional<String> error = emqxManagementClient.publish(publishRequest);
    if (error.isPresent()) {
      if (sync) {
        replyAwaiter.clear(key);
      }
      return ResponseEntity.ok(fail(requestId, error.get()));
    }

    if (!sync) {
      return ResponseEntity.ok(success(requestId, messageId, null));
    }

    try {
      String reply = replyAwaiter.await(pending, timeoutMs);
      return ResponseEntity.ok(success(requestId, messageId, reply));
    } catch (TimeoutException e) {
      return ResponseEntity.ok(fail(requestId, "invoke timeout after " + timeoutMs + "ms"));
    } catch (Exception e) {
      return ResponseEntity.ok(fail(requestId, "invoke failed: " + e.getMessage()));
    } finally {
      replyAwaiter.clear(key);
    }
  }

  private String buildPayload(String messageId, String identifier, String argsJsonString) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("id", messageId);
    root.put("version", "1.0");
    root.put("method", "thing.service." + identifier + ".invoke");
    root.set("params", parseArgs(argsJsonString));
    return root.toString();
  }

  private JsonNode parseArgs(String argsJsonString) {
    String raw = safe(argsJsonString);
    if (raw.isEmpty()) {
      return objectMapper.createObjectNode();
    }
    try {
      JsonNode node = objectMapper.readTree(raw);
      if (node != null && node.isObject()) {
        return node;
      }
      throw new IllegalArgumentException("args must be a JSON object");
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("args is invalid JSON object: " + e.getMessage());
    }
  }

  private static InvokeThingServiceResponse success(String requestId, String messageId, String result) {
    InvokeThingServiceResponse response = new InvokeThingServiceResponse();
    response.setSuccess(Boolean.TRUE);
    response.setRequestId(requestId);
    InvokeThingServiceResponse.DataPayload data = new InvokeThingServiceResponse.DataPayload();
    data.setMessageId(messageId);
    data.setResult(result);
    response.setData(data);
    return response;
  }

  private static InvokeThingServiceResponse fail(String requestId, String errorMessage) {
    InvokeThingServiceResponse response = new InvokeThingServiceResponse();
    response.setSuccess(Boolean.FALSE);
    response.setRequestId(requestId);
    response.setErrorMessage(errorMessage);
    return response;
  }

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private void persistInvokeRecord(String productKey, String deviceName, String identifier,
                                   String traceId, String messageId, DownstreamPublishRequest request) {
    DeviceServiceRecordEntity record = new DeviceServiceRecordEntity();
    record.setTenantId(resolveTenantId(productKey));
    record.setOrgId(resolveOrgId(productKey));
    record.setProductKey(productKey);
    record.setDeviceName(deviceName);
    record.setServiceName(identifier);
    record.setDirection("request");
    record.setTraceId(traceId);
    record.setMessageId(messageId);
    record.setTopic(request.getTopic());
    record.setPayload(request.getPayload());
    deviceRecordMapper.insertServiceRecord(record);
  }

  private Long resolveTenantId(String productKey) {
    ProductEntity product = productMapper.findByProductKey(productKey);
    if (product != null && product.getTenantId() != null) {
      return product.getTenantId();
    }
    return DataDomainContext.getTenantId();
  }

  private Long resolveOrgId(String productKey) {
    ProductEntity product = productMapper.findByProductKey(productKey);
    if (product != null && product.getOrgId() != null) {
      return product.getOrgId();
    }
    return DataDomainContext.getOrgId();
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private static int normalizeTimeout(Integer timeoutMs) {
    if (timeoutMs == null) {
      return 8000;
    }
    return Math.max(1000, Math.min(timeoutMs, 30000));
  }
}

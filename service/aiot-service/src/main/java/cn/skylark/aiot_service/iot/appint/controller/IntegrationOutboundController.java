package cn.skylark.aiot_service.iot.appint.controller;

import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelCreateRequest;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelPageQuery;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelPageResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundChannelUpdateRequest;
import cn.skylark.aiot_service.iot.appint.dto.OutboundDeliveryPageQuery;
import cn.skylark.aiot_service.iot.appint.dto.OutboundDeliveryPageResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionCreateRequest;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionPageQuery;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionPageResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionResponse;
import cn.skylark.aiot_service.iot.appint.dto.OutboundSubscriptionUpdateRequest;
import cn.skylark.aiot_service.iot.appint.dto.TestWebhookResponse;
import cn.skylark.aiot_service.iot.appint.service.IntegrationOutboundMgmtService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/aiot-service/mgmt/integration")
public class IntegrationOutboundController {

  private final IntegrationOutboundMgmtService integrationOutboundMgmtService;

  public IntegrationOutboundController(IntegrationOutboundMgmtService integrationOutboundMgmtService) {
    this.integrationOutboundMgmtService = integrationOutboundMgmtService;
  }

  @GetMapping("/event-types")
  public List<String> eventTypes() {
    return integrationOutboundMgmtService.listEventTypes();
  }

  @GetMapping("/channels")
  public OutboundChannelPageResponse listChannels(@ModelAttribute OutboundChannelPageQuery query) {
    return integrationOutboundMgmtService.listChannels(query);
  }

  @PostMapping("/channels")
  public OutboundChannelResponse createChannel(@Validated @RequestBody OutboundChannelCreateRequest request) {
    return integrationOutboundMgmtService.createChannel(request);
  }

  @GetMapping("/channels/{id}")
  public OutboundChannelResponse getChannel(@PathVariable("id") Long id) {
    return integrationOutboundMgmtService.getChannel(id);
  }

  @PutMapping("/channels/{id}")
  public OutboundChannelResponse updateChannel(@PathVariable("id") Long id,
                                               @Validated @RequestBody OutboundChannelUpdateRequest request) {
    return integrationOutboundMgmtService.updateChannel(id, request);
  }

  @DeleteMapping("/channels/{id}")
  public void deleteChannel(@PathVariable("id") Long id) {
    integrationOutboundMgmtService.deleteChannel(id);
  }

  @PostMapping("/channels/{id}/test")
  public TestWebhookResponse testChannel(@PathVariable("id") Long id) {
    return integrationOutboundMgmtService.testChannel(id);
  }

  @GetMapping("/subscriptions")
  public OutboundSubscriptionPageResponse listSubscriptions(@ModelAttribute OutboundSubscriptionPageQuery query) {
    return integrationOutboundMgmtService.listSubscriptions(query);
  }

  @PostMapping("/subscriptions")
  public OutboundSubscriptionResponse createSubscription(@Validated @RequestBody OutboundSubscriptionCreateRequest request) {
    return integrationOutboundMgmtService.createSubscription(request);
  }

  @GetMapping("/subscriptions/{id}")
  public OutboundSubscriptionResponse getSubscription(@PathVariable("id") Long id) {
    return integrationOutboundMgmtService.getSubscription(id);
  }

  @PutMapping("/subscriptions/{id}")
  public OutboundSubscriptionResponse updateSubscription(@PathVariable("id") Long id,
                                                         @Validated @RequestBody OutboundSubscriptionUpdateRequest request) {
    return integrationOutboundMgmtService.updateSubscription(id, request);
  }

  @DeleteMapping("/subscriptions/{id}")
  public void deleteSubscription(@PathVariable("id") Long id) {
    integrationOutboundMgmtService.deleteSubscription(id);
  }

  @GetMapping("/deliveries")
  public OutboundDeliveryPageResponse listDeliveries(@ModelAttribute OutboundDeliveryPageQuery query) {
    return integrationOutboundMgmtService.listDeliveries(query);
  }

  @PostMapping("/deliveries/{id}/retry")
  public void retryDelivery(@PathVariable("id") Long id) {
    integrationOutboundMgmtService.retryDelivery(id);
  }
}

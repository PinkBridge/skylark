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
import cn.skylark.web.common.Ret;
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
@RequestMapping("/api/aiot-service/appint")
public class IntegrationOutboundController {

  private final IntegrationOutboundMgmtService integrationOutboundMgmtService;

  public IntegrationOutboundController(IntegrationOutboundMgmtService integrationOutboundMgmtService) {
    this.integrationOutboundMgmtService = integrationOutboundMgmtService;
  }

  @GetMapping("/event-types")
  public Ret<List<String>> eventTypes() {
    return Ret.data(integrationOutboundMgmtService.listEventTypes());
  }

  @GetMapping("/channels")
  public Ret<OutboundChannelPageResponse> listChannels(@ModelAttribute OutboundChannelPageQuery query) {
    return Ret.data(integrationOutboundMgmtService.listChannels(query));
  }

  @PostMapping("/channels")
  public Ret<OutboundChannelResponse> createChannel(@Validated @RequestBody OutboundChannelCreateRequest request) {
    return Ret.data(integrationOutboundMgmtService.createChannel(request));
  }

  @GetMapping("/channels/{id}")
  public Ret<OutboundChannelResponse> getChannel(@PathVariable("id") Long id) {
    return Ret.data(integrationOutboundMgmtService.getChannel(id));
  }

  @PutMapping("/channels/{id}")
  public Ret<OutboundChannelResponse> updateChannel(@PathVariable("id") Long id,
                                               @Validated @RequestBody OutboundChannelUpdateRequest request) {
    return Ret.data(integrationOutboundMgmtService.updateChannel(id, request));
  }

  @DeleteMapping("/channels/{id}")
  public Ret<Void> deleteChannel(@PathVariable("id") Long id) {
    integrationOutboundMgmtService.deleteChannel(id);
    return Ret.ok();
  }

  @PostMapping("/channels/{id}/test")
  public Ret<TestWebhookResponse> testChannel(@PathVariable("id") Long id) {
    return Ret.data(integrationOutboundMgmtService.testChannel(id));
  }

  @GetMapping("/subscriptions")
  public Ret<OutboundSubscriptionPageResponse> listSubscriptions(@ModelAttribute OutboundSubscriptionPageQuery query) {
    return Ret.data(integrationOutboundMgmtService.listSubscriptions(query));
  }

  @PostMapping("/subscriptions")
  public Ret<OutboundSubscriptionResponse> createSubscription(@Validated @RequestBody OutboundSubscriptionCreateRequest request) {
    return Ret.data(integrationOutboundMgmtService.createSubscription(request));
  }

  @GetMapping("/subscriptions/{id}")
  public Ret<OutboundSubscriptionResponse> getSubscription(@PathVariable("id") Long id) {
    return Ret.data(integrationOutboundMgmtService.getSubscription(id));
  }

  @PutMapping("/subscriptions/{id}")
  public Ret<OutboundSubscriptionResponse> updateSubscription(@PathVariable("id") Long id,
                                                         @Validated @RequestBody OutboundSubscriptionUpdateRequest request) {
    return Ret.data(integrationOutboundMgmtService.updateSubscription(id, request));
  }

  @DeleteMapping("/subscriptions/{id}")
  public Ret<Void> deleteSubscription(@PathVariable("id") Long id) {
    integrationOutboundMgmtService.deleteSubscription(id);
    return Ret.ok();
  }

  @GetMapping("/deliveries")
  public Ret<OutboundDeliveryPageResponse> listDeliveries(@ModelAttribute OutboundDeliveryPageQuery query) {
    return Ret.data(integrationOutboundMgmtService.listDeliveries(query));
  }

  @PostMapping("/deliveries/{id}/retry")
  public Ret<Void> retryDelivery(@PathVariable("id") Long id) {
    integrationOutboundMgmtService.retryDelivery(id);
    return Ret.ok();
  }
}

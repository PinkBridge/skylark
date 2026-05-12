package cn.skylark.aiot_service.iot.notification.controller;

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
import cn.skylark.aiot_service.iot.notification.service.NotificationMgmtService;
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
@RequestMapping("/api/aiot-service/notification")
public class NotificationMgmtController {
  private final NotificationMgmtService notificationMgmtService;

  public NotificationMgmtController(NotificationMgmtService notificationMgmtService) {
    this.notificationMgmtService = notificationMgmtService;
  }

  @GetMapping("/event-types")
  public Ret<List<String>> eventTypes() {
    return Ret.data(notificationMgmtService.listEventTypes());
  }

  @GetMapping("/channels")
  public Ret<NotificationChannelPageResponse> listChannels(@ModelAttribute NotificationChannelPageQuery query) {
    return Ret.data(notificationMgmtService.listChannels(query));
  }

  @PostMapping("/channels")
  public Ret<NotificationChannelResponse> createChannel(@Validated @RequestBody NotificationChannelCreateRequest req) {
    return Ret.data(notificationMgmtService.createChannel(req));
  }

  @GetMapping("/channels/{id}")
  public Ret<NotificationChannelResponse> getChannel(@PathVariable("id") Long id) {
    return Ret.data(notificationMgmtService.getChannel(id));
  }

  @PutMapping("/channels/{id}")
  public Ret<NotificationChannelResponse> updateChannel(@PathVariable("id") Long id, @Validated @RequestBody NotificationChannelUpdateRequest req) {
    return Ret.data(notificationMgmtService.updateChannel(id, req));
  }

  @DeleteMapping("/channels/{id}")
  public Ret<Void> deleteChannel(@PathVariable("id") Long id) {
    notificationMgmtService.deleteChannel(id);
    return Ret.ok();
  }

  @PostMapping("/channels/{id}/test")
  public Ret<NotificationTestResult> testChannel(@PathVariable("id") Long id) {
    return Ret.data(notificationMgmtService.testChannel(id));
  }

  @GetMapping("/subscriptions")
  public Ret<NotificationSubscriptionPageResponse> listSubscriptions(@ModelAttribute NotificationSubscriptionPageQuery query) {
    return Ret.data(notificationMgmtService.listSubscriptions(query));
  }

  @PostMapping("/subscriptions")
  public Ret<NotificationSubscriptionResponse> createSubscription(@Validated @RequestBody NotificationSubscriptionCreateRequest req) {
    return Ret.data(notificationMgmtService.createSubscription(req));
  }

  @GetMapping("/subscriptions/{id}")
  public Ret<NotificationSubscriptionResponse> getSubscription(@PathVariable("id") Long id) {
    return Ret.data(notificationMgmtService.getSubscription(id));
  }

  @PutMapping("/subscriptions/{id}")
  public Ret<NotificationSubscriptionResponse> updateSubscription(@PathVariable("id") Long id, @Validated @RequestBody NotificationSubscriptionUpdateRequest req) {
    return Ret.data(notificationMgmtService.updateSubscription(id, req));
  }

  @DeleteMapping("/subscriptions/{id}")
  public Ret<Void> deleteSubscription(@PathVariable("id") Long id) {
    notificationMgmtService.deleteSubscription(id);
    return Ret.ok();
  }

  @GetMapping("/deliveries")
  public Ret<NotificationDeliveryPageResponse> listDeliveries(@ModelAttribute NotificationDeliveryPageQuery query) {
    return Ret.data(notificationMgmtService.listDeliveries(query));
  }

  @GetMapping("/deliveries/{id}")
  public Ret<NotificationDeliveryResponse> getDelivery(@PathVariable("id") Long id) {
    return Ret.data(notificationMgmtService.getDelivery(id));
  }

  @PostMapping("/deliveries/{id}/retry")
  public Ret<Void> retryDelivery(@PathVariable("id") Long id) {
    notificationMgmtService.retryDelivery(id);
    return Ret.ok();
  }
}


package cn.skylark.aiot_service.iot.integration;

import cn.skylark.aiot_service.iot.integration.model.NormalizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class IntegrationEventListener {
  private static final Logger log = LoggerFactory.getLogger(IntegrationEventListener.class);

  private final OutboundDispatchService outboundDispatchService;

  public IntegrationEventListener(OutboundDispatchService outboundDispatchService) {
    this.outboundDispatchService = outboundDispatchService;
  }

  @Async
  @EventListener
  public void onNormalizedEvent(NormalizedEvent event) {
    try {
      outboundDispatchService.dispatch(event);
    } catch (RuntimeException e) {
      log.warn("integration dispatch failed eventId={} type={}", event.getEventId(), event.getEventType(), e);
    }
  }
}

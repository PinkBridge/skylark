package cn.skylark.aiot_service.iot.integration;

import cn.skylark.aiot_service.iot.integration.model.NormalizedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NormalizedEventPublisher {
  private final ApplicationEventPublisher applicationEventPublisher;

  public NormalizedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  public void publish(NormalizedEvent event) {
    if (event == null) {
      return;
    }
    applicationEventPublisher.publishEvent(event);
  }
}

package cn.skylark.aiot_service.iot.notification;

import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import cn.skylark.aiot_service.iot.notification.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
  private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

  private final NotificationDispatchService notificationDispatchService;

  public NotificationEventListener(NotificationDispatchService notificationDispatchService) {
    this.notificationDispatchService = notificationDispatchService;
  }

  @Async
  @EventListener
  public void onNormalizedEvent(NormalizedEvent event) {
    try {
      notificationDispatchService.dispatch(event);
    } catch (RuntimeException e) {
      log.warn("notification dispatch failed eventId={} type={}",
          event == null ? null : event.getEventId(),
          event == null ? null : event.getEventType(), e);
    }
  }
}

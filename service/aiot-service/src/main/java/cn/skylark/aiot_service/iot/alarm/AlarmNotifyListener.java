package cn.skylark.aiot_service.iot.alarm;

import cn.skylark.aiot_service.iot.alarm.service.AlarmNotifyDispatchService;
import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AlarmNotifyListener {
  private static final Logger log = LoggerFactory.getLogger(AlarmNotifyListener.class);

  private final AlarmNotifyDispatchService alarmNotifyDispatchService;

  public AlarmNotifyListener(AlarmNotifyDispatchService alarmNotifyDispatchService) {
    this.alarmNotifyDispatchService = alarmNotifyDispatchService;
  }

  @Async
  @EventListener
  public void onNormalizedEvent(NormalizedEvent event) {
    if (event == null) {
      return;
    }
    String t = event.getEventType();
    if (!"alarm.triggered".equals(t) && !"alarm.recovered".equals(t)) {
      return;
    }
    try {
      alarmNotifyDispatchService.dispatchAlarmEvent(event);
    } catch (RuntimeException e) {
      log.warn("alarm notify dispatch failed eventId={} type={}", event.getEventId(), event.getEventType(), e);
    }
  }
}

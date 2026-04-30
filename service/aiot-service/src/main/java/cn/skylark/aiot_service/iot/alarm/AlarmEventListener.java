package cn.skylark.aiot_service.iot.alarm;

import cn.skylark.aiot_service.iot.appint.model.NormalizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AlarmEventListener {
  private static final Logger log = LoggerFactory.getLogger(AlarmEventListener.class);

  private final AlarmEvaluatorService alarmEvaluatorService;

  public AlarmEventListener(AlarmEvaluatorService alarmEvaluatorService) {
    this.alarmEvaluatorService = alarmEvaluatorService;
  }

  @Async
  @EventListener
  public void onNormalizedEvent(NormalizedEvent event) {
    try {
      alarmEvaluatorService.evaluate(event);
    } catch (RuntimeException e) {
      log.warn("alarm evaluate failed eventId={} type={}", event == null ? null : event.getEventId(),
          event == null ? null : event.getEventType(), e);
    }
  }
}


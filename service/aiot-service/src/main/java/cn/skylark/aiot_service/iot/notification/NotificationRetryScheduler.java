package cn.skylark.aiot_service.iot.notification;

import cn.skylark.aiot_service.iot.notification.entity.IotNotificationDeliveryEntity;
import cn.skylark.aiot_service.iot.notification.mapper.IotNotificationDeliveryMapper;
import cn.skylark.aiot_service.iot.notification.service.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationRetryScheduler {
  private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

  private final IotNotificationDeliveryMapper deliveryMapper;
  private final NotificationDispatchService notificationDispatchService;

  @Value("${iot.notification.retry.max-attempts:8}")
  private int maxAttempts;

  public NotificationRetryScheduler(IotNotificationDeliveryMapper deliveryMapper,
                                    NotificationDispatchService notificationDispatchService) {
    this.deliveryMapper = deliveryMapper;
    this.notificationDispatchService = notificationDispatchService;
  }

  @Scheduled(fixedDelayString = "${iot.notification.retry-delay-ms:30000}")
  public void retryDueDeliveries() {
    List<IotNotificationDeliveryEntity> due = deliveryMapper.listDueRetry(40, maxAttempts);
    if (due == null || due.isEmpty()) {
      return;
    }
    for (IotNotificationDeliveryEntity d : due) {
      try {
        notificationDispatchService.resendDelivery(d);
      } catch (RuntimeException e) {
        log.warn("notification retry failed deliveryId={}", d.getId(), e);
      }
    }
  }
}

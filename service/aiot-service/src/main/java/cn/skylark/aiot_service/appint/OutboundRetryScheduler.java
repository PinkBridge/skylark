package cn.skylark.aiot_service.appint;

import cn.skylark.aiot_service.appint.mapper.IotOutboundChannelMapper;
import cn.skylark.aiot_service.appint.mapper.IotOutboundDeliveryMapper;
import cn.skylark.aiot_service.appint.entity.IotOutboundChannelEntity;
import cn.skylark.aiot_service.appint.entity.IotOutboundDeliveryEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class OutboundRetryScheduler {
  private static final Logger log = LoggerFactory.getLogger(OutboundRetryScheduler.class);

  private final IotOutboundDeliveryMapper deliveryMapper;
  private final IotOutboundChannelMapper channelMapper;
  private final WebhookOutboundClient webhookOutboundClient;
  private final ObjectMapper objectMapper;

  @Value("${iot.appint.retry.max-attempts:8}")
  private int maxAttempts;

  public OutboundRetryScheduler(IotOutboundDeliveryMapper deliveryMapper,
                                IotOutboundChannelMapper channelMapper,
                                WebhookOutboundClient webhookOutboundClient,
                                ObjectMapper objectMapper) {
    this.deliveryMapper = deliveryMapper;
    this.channelMapper = channelMapper;
    this.webhookOutboundClient = webhookOutboundClient;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelayString = "${iot.appint.retry-delay-ms:30000}")
  public void retryDueDeliveries() {
    List<IotOutboundDeliveryEntity> due = deliveryMapper.listDueRetry(40, maxAttempts);
    if (due == null || due.isEmpty()) {
      return;
    }
    for (IotOutboundDeliveryEntity d : due) {
      try {
        retryOne(d);
      } catch (RuntimeException e) {
        log.warn("integration retry failed deliveryId={}", d.getId(), e);
      }
    }
  }

  private void retryOne(IotOutboundDeliveryEntity d) {
    IotOutboundChannelEntity ch = channelMapper.findById(d.getChannelId());
    if (ch == null || ch.getEnabled() == null || ch.getEnabled() == 0) {
      return;
    }
    if (!"WEBHOOK".equalsIgnoreCase(ch.getType() == null ? "" : ch.getType().trim())) {
      return;
    }
    if (!StringUtils.hasText(d.getPayloadSnapshot())) {
      return;
    }
    OutboundDispatchService.WebhookChannelConfig cfg =
        OutboundDispatchService.WebhookChannelConfig.parse(ch.getConfigJson(), objectMapper);
    if (!cfg.isValid()) {
      return;
    }
    int prev = d.getAttempts() == null ? 0 : d.getAttempts();
    WebhookOutboundClient.WebhookSendResult result =
        webhookOutboundClient.postJson(cfg.url, d.getPayloadSnapshot(), cfg.signingSecret, cfg.readTimeoutMs);
    int nextAttempt = prev + 1;
    d.setAttempts(nextAttempt);
    if (result.isOk()) {
      d.setStatus("success");
      d.setHttpStatus(result.getHttpStatus());
      d.setLastError(null);
      d.setNextRetryAt(null);
    } else {
      d.setHttpStatus(result.getHttpStatus() > 0 ? result.getHttpStatus() : null);
      d.setLastError(result.getError());
      if (nextAttempt >= maxAttempts) {
        d.setStatus("dead");
        d.setNextRetryAt(null);
      } else {
        d.setStatus("failed");
        d.setNextRetryAt(OutboundDispatchService.nextRetryTime(nextAttempt));
      }
    }
    deliveryMapper.update(d);
  }
}

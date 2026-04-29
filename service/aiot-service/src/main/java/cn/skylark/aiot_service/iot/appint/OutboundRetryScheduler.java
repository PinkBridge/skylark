package cn.skylark.aiot_service.iot.appint;

import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundChannelMapper;
import cn.skylark.aiot_service.iot.appint.mapper.IotOutboundDeliveryMapper;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundChannelEntity;
import cn.skylark.aiot_service.iot.appint.entity.IotOutboundDeliveryEntity;
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
  private final MqttOutboundClient mqttOutboundClient;
  private final ObjectMapper objectMapper;

  @Value("${iot.appint.retry.max-attempts:8}")
  private int maxAttempts;

  public OutboundRetryScheduler(IotOutboundDeliveryMapper deliveryMapper,
                                IotOutboundChannelMapper channelMapper,
                                WebhookOutboundClient webhookOutboundClient,
                                MqttOutboundClient mqttOutboundClient,
                                ObjectMapper objectMapper) {
    this.deliveryMapper = deliveryMapper;
    this.channelMapper = channelMapper;
    this.webhookOutboundClient = webhookOutboundClient;
    this.mqttOutboundClient = mqttOutboundClient;
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
    if (!StringUtils.hasText(d.getPayloadSnapshot())) {
      return;
    }
    int prev = d.getAttempts() == null ? 0 : d.getAttempts();
    String type = ch.getType() == null ? "" : ch.getType().trim();
    boolean ok;
    Integer httpStatus = null;
    String err = null;

    if ("WEBHOOK".equalsIgnoreCase(type)) {
      OutboundDispatchService.WebhookChannelConfig cfg =
          OutboundDispatchService.WebhookChannelConfig.parse(ch.getConfigJson(), objectMapper);
      if (!cfg.isValid()) {
        return;
      }
      WebhookOutboundClient.WebhookSendResult result =
          webhookOutboundClient.postJson(cfg.url, d.getPayloadSnapshot(), cfg.signingSecret, cfg.readTimeoutMs);
      ok = result.isOk();
      httpStatus = result.getHttpStatus() > 0 ? result.getHttpStatus() : null;
      err = result.getError();
    } else if ("MQTT".equalsIgnoreCase(type)) {
      OutboundDispatchService.MqttChannelConfig cfg =
          OutboundDispatchService.MqttChannelConfig.parse(ch.getConfigJson(), objectMapper);
      if (!cfg.isValid()) {
        return;
      }
      MqttOutboundClient.PublishResult result =
          mqttOutboundClient.publishJson(cfg.brokerUrl, cfg.clientId, cfg.username, cfg.password, cfg.topic, cfg.qos, d.getPayloadSnapshot());
      ok = result.isOk();
      err = result.getError();
    } else {
      return;
    }

    int nextAttempt = prev + 1;
    d.setAttempts(nextAttempt);
    if (ok) {
      d.setStatus("success");
      d.setHttpStatus(httpStatus);
      d.setLastError(null);
      d.setNextRetryAt(null);
    } else {
      d.setHttpStatus(httpStatus);
      d.setLastError(err);
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

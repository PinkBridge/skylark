package cn.skylark.aiot_service.iot.appint;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class MqttOutboundClient {

  public PublishResult publishJson(String brokerUrl,
                                   String clientId,
                                   String username,
                                   String password,
                                   String topic,
                                   int qos,
                                   String jsonBody) {
    if (!StringUtils.hasText(brokerUrl) || !StringUtils.hasText(topic)) {
      return PublishResult.failure("mqtt brokerUrl/topic required");
    }
    String cid = StringUtils.hasText(clientId) ? clientId.trim() : ("skylark-appint-" + UUID.randomUUID());
    String payload = jsonBody == null ? "" : jsonBody;
    int q = Math.max(0, Math.min(qos, 2));

    MqttClient client = null;
    try {
      client = new MqttClient(brokerUrl.trim(), cid, new MemoryPersistence());
      MqttConnectOptions opt = new MqttConnectOptions();
      opt.setCleanSession(true);
      opt.setAutomaticReconnect(false);
      opt.setConnectionTimeout(10);
      opt.setKeepAliveInterval(30);
      if (StringUtils.hasText(username)) {
        opt.setUserName(username.trim());
      }
      if (password != null) {
        opt.setPassword(password.toCharArray());
      }
      client.connect(opt);

      MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
      msg.setQos(q);
      msg.setRetained(false);
      client.publish(topic.trim(), msg);
      return PublishResult.ok();
    } catch (MqttException e) {
      return PublishResult.failure("mqtt publish failed: " + safeMsg(e));
    } finally {
      if (client != null) {
        try {
          if (client.isConnected()) {
            client.disconnect();
          }
          client.close();
        } catch (Exception ignore) {
          // ignore close errors
        }
      }
    }
  }

  private static String safeMsg(Throwable t) {
    String m = t == null ? null : t.getMessage();
    return m == null ? t.getClass().getSimpleName() : m;
  }

  public static final class PublishResult {
    private final boolean ok;
    private final String error;

    private PublishResult(boolean ok, String error) {
      this.ok = ok;
      this.error = error;
    }

    public static PublishResult ok() {
      return new PublishResult(true, null);
    }

    public static PublishResult failure(String error) {
      return new PublishResult(false, error);
    }

    public boolean isOk() {
      return ok;
    }

    public String getError() {
      return error;
    }
  }
}


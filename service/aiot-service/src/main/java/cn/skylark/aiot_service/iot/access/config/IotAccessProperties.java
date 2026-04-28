package cn.skylark.aiot_service.iot.access.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "iot.access")
public class IotAccessProperties {

  private List<DeviceCredential> devices = new ArrayList<DeviceCredential>();
  private EmqxManagement emqx = new EmqxManagement();
  private Auth auth = new Auth();
  private Acl acl = new Acl();
  private Webhook webhook = new Webhook();

  @Data
  public static class Webhook {
    private boolean enabled = false;
    private String secret = "";
  }

  @Data
  public static class Auth {
    private boolean useDb = true;
  }

  @Data
  public static class Acl {
    private boolean useDb = false;
  }

  @Data
  public static class EmqxManagement {
    private String baseUrl = "http://emqx:18083";
    private String apiKey = "";
    private String apiSecret = "";
    private String dashboardUsername = "";
    private String dashboardPassword = "";
  }

  @Data
  public static class DeviceCredential {
    private String deviceId;
    private String username;
    private String password;
    private boolean enabled = true;
    private String publishPrefix = "up/";
    private String subscribePrefix = "down/";
  }
}


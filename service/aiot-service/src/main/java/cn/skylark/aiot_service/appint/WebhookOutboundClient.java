package cn.skylark.aiot_service.appint;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class WebhookOutboundClient {

  public WebhookSendResult postJson(String url, String jsonBody, String signingSecret, int readTimeoutMs) {
    if (!StringUtils.hasText(url)) {
      return WebhookSendResult.failure(0, "url empty");
    }
    String ts = String.valueOf(System.currentTimeMillis());
    String signPayload = ts + "." + jsonBody;
    String sig = WebhookSignatureUtil.hmacSha256Hex(signingSecret == null ? "" : signingSecret, signPayload);

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
    headers.set("X-Skylark-Timestamp", ts);
    if (StringUtils.hasText(signingSecret)) {
      headers.set("X-Skylark-Signature", sig);
    }

    HttpEntity<String> entity = new HttpEntity<String>(jsonBody, headers);
    RestTemplate client = client(readTimeoutMs);
    try {
      ResponseEntity<String> resp = client.exchange(url, HttpMethod.POST, entity, String.class);
      int code = resp.getStatusCodeValue();
      if (code >= 200 && code < 300) {
        return WebhookSendResult.success(code);
      }
      return WebhookSendResult.failure(code, "HTTP " + code);
    } catch (RestClientException e) {
      String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
      if (msg.length() > 900) {
        msg = msg.substring(0, 900);
      }
      return WebhookSendResult.failure(0, msg);
    }
  }

  private static RestTemplate client(int readTimeoutMs) {
    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(10_000);
    int rt = readTimeoutMs <= 0 ? 30_000 : Math.min(readTimeoutMs, 120_000);
    f.setReadTimeout(rt);
    return new RestTemplate(f);
  }

  public static final class WebhookSendResult {
    private final boolean ok;
    private final int httpStatus;
    private final String error;

    private WebhookSendResult(boolean ok, int httpStatus, String error) {
      this.ok = ok;
      this.httpStatus = httpStatus;
      this.error = error;
    }

    static WebhookSendResult success(int httpStatus) {
      return new WebhookSendResult(true, httpStatus, null);
    }

    static WebhookSendResult failure(int httpStatus, String error) {
      return new WebhookSendResult(false, httpStatus, error);
    }

    public boolean isOk() {
      return ok;
    }

    public int getHttpStatus() {
      return httpStatus;
    }

    public String getError() {
      return error;
    }
  }
}

package cn.skylark.aiot_service.iot.access.service;

import cn.skylark.aiot_service.iot.access.config.IotAccessProperties;
import cn.skylark.aiot_service.iot.access.model.DownstreamPublishRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EmqxManagementHttpClient implements EmqxManagementClient {
  private static final Logger log = LoggerFactory.getLogger(EmqxManagementHttpClient.class);

  private final IotAccessProperties properties;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public EmqxManagementHttpClient(IotAccessProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.restTemplate = new RestTemplate();
  }

  @Override
  public Optional<String> publish(DownstreamPublishRequest req) {
    String baseUrl = properties.getEmqx() == null ? "" : safe(properties.getEmqx().getBaseUrl());
    if (!StringUtils.hasText(baseUrl)) {
      return Optional.of("emqx baseUrl is empty");
    }
    String url = baseUrl.endsWith("/") ? (baseUrl + "api/v5/publish") : (baseUrl + "/api/v5/publish");

    Map<String, Object> body = new HashMap<String, Object>();
    body.put("topic", req.getTopic());
    body.put("payload", req.getPayload() == null ? "" : req.getPayload());
    body.put("qos", req.getQos() == null ? 1 : req.getQos());
    body.put("retain", req.getRetain() != null && req.getRetain());

    HttpHeaders basicHeaders = new HttpHeaders();
    basicHeaders.setContentType(MediaType.APPLICATION_JSON);
    String apiKey = properties.getEmqx() == null ? "" : safe(properties.getEmqx().getApiKey());
    String apiSecret = properties.getEmqx() == null ? "" : safe(properties.getEmqx().getApiSecret());
    if (StringUtils.hasText(apiKey) && StringUtils.hasText(apiSecret)) {
      basicHeaders.setBasicAuth(apiKey, apiSecret);
    }

    try {
      try {
        return doPublish(url, body, basicHeaders);
      } catch (HttpStatusCodeException e) {
        if (e.getRawStatusCode() != 401) {
          return Optional.of("emqx publish failed: " + e.getStatusCode() + ", " + abbreviate(e.getResponseBodyAsString(), 300));
        }
        String token = loginDashboardAndGetToken(baseUrl);
        if (!StringUtils.hasText(token)) {
          return Optional.of("emqx publish 401 and token login failed");
        }
        HttpHeaders bearerHeaders = new HttpHeaders();
        bearerHeaders.setContentType(MediaType.APPLICATION_JSON);
        bearerHeaders.setBearerAuth(token);
        return doPublish(url, body, bearerHeaders);
      }
    } catch (RestClientException e) {
      return Optional.of("emqx publish request failed: " + safe(e.getMessage()));
    } catch (Exception e) {
      return Optional.of("emqx publish unexpected error: " + safe(e.getMessage()));
    }
  }

  private Optional<String> doPublish(String url, Map<String, Object> body, HttpHeaders headers) throws Exception {
    ResponseEntity<String> resp = restTemplate.postForEntity(url, new HttpEntity<Map<String, Object>>(body, headers), String.class);
    if (!resp.getStatusCode().is2xxSuccessful()) {
      return Optional.of("emqx publish http not 2xx: " + resp.getStatusCode());
    }
    String raw = resp.getBody() == null ? "" : resp.getBody();
    return parseErrorIfAny(raw);
  }

  private String loginDashboardAndGetToken(String baseUrl) {
    String username = properties.getEmqx() == null ? "" : safe(properties.getEmqx().getDashboardUsername());
    String password = properties.getEmqx() == null ? "" : safe(properties.getEmqx().getDashboardPassword());
    if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
      return "";
    }
    String loginUrl = baseUrl.endsWith("/") ? (baseUrl + "api/v5/login") : (baseUrl + "/api/v5/login");
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      Map<String, String> payload = new HashMap<String, String>();
      payload.put("username", username);
      payload.put("password", password);
      ResponseEntity<String> resp = restTemplate.postForEntity(loginUrl, new HttpEntity<Map<String, String>>(payload, headers), String.class);
      if (!resp.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(resp.getBody())) {
        return "";
      }
      JsonNode root = objectMapper.readTree(resp.getBody());
      return safe(root.path("token").asText(""));
    } catch (Exception e) {
      log.warn("EMQX dashboard login fallback failed: {}", e.getMessage());
      return "";
    }
  }

  private Optional<String> parseErrorIfAny(String raw) {
    if (!StringUtils.hasText(raw)) {
      return Optional.empty();
    }
    try {
      JsonNode root = objectMapper.readTree(raw);
      if (root.has("code")) {
        int code = root.path("code").asInt(0);
        if (code == 0) {
          return Optional.empty();
        }
        return Optional.of("emqx publish business error code=" + code + ", body=" + abbreviate(raw, 300));
      }
    } catch (Exception ignore) {
      // ignore non-json response
    }
    return Optional.empty();
  }

  private static String safe(String s) {
    return s == null ? "" : s.trim();
  }

  private static String abbreviate(String s, int max) {
    if (s == null || s.length() <= max) {
      return s;
    }
    return s.substring(0, max) + "...";
  }
}


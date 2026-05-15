package cn.skylark.aiot_service.iot.notify.service;

import cn.skylark.aiot_service.iot.mgmt.service.MgmtException;
import cn.skylark.aiot_service.iot.notify.entity.IotNotifyChannelEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

@Component
public class NotifyChannelSender {
  private static final Logger log = LoggerFactory.getLogger(NotifyChannelSender.class);
  private static final String ALIYUN_SMS_ENDPOINT = "https://dysmsapi.aliyuncs.com";

  private final ObjectMapper objectMapper;

  public NotifyChannelSender(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void sendTestEmail(IotNotifyChannelEntity ch, String to) {
    sendMailFromChannel(ch, to, "Skylark notify test", "This is a test email from IoT notification channel.");
  }

  public void sendMailFromChannel(IotNotifyChannelEntity ch, String to, String subject, String text) {
    if (!StringUtils.hasText(to)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "recipient email required");
    }
    JsonNode cfg = requireJson(ch);
    String host = text(cfg, "smtpHost");
    Integer port = cfg.hasNonNull("smtpPort") && cfg.get("smtpPort").isNumber() ? cfg.get("smtpPort").asInt() : null;
    String user = text(cfg, "smtpUsername");
    String pass = text(cfg, "smtpPassword");
    String from = text(cfg, "mailFrom");
    if (!StringUtils.hasText(host) || port == null || !StringUtils.hasText(user) || !StringUtils.hasText(pass) || !StringUtils.hasText(from)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "mail channel not fully configured");
    }
    boolean ssl = !cfg.has("smtpSsl") || cfg.get("smtpSsl").asBoolean(true);
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(host.trim());
    sender.setPort(port);
    sender.setUsername(user.trim());
    sender.setPassword(pass);
    sender.getJavaMailProperties().put("mail.transport.protocol", "smtp");
    sender.getJavaMailProperties().put("mail.smtp.auth", "true");
    if (ssl) {
      sender.getJavaMailProperties().put("mail.smtp.ssl.enable", "true");
      sender.getJavaMailProperties().put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      sender.getJavaMailProperties().put("mail.smtp.socketFactory.port", String.valueOf(port));
      sender.getJavaMailProperties().put("mail.smtp.ssl.trust", host.trim());
    } else {
      sender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");
    }
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from.trim());
    msg.setTo(to.trim());
    msg.setSubject(subject);
    msg.setText(text);
    try {
      sender.send(msg);
    } catch (Exception e) {
      log.warn("sendMail failed to={}", to, e);
      throw new MgmtException(HttpStatus.BAD_REQUEST, formatSmtpSendFailure(host, e));
    }
  }

  /** Best-effort deepest message + short hint for common provider errors (e.g. NetEase 550). */
  private static String formatSmtpSendFailure(String smtpHost, Exception e) {
    String detail = deepestMessage(e);
    StringBuilder sb = new StringBuilder("send mail failed: ").append(detail);
    String hint = smtpAuthHint(smtpHost, detail);
    if (StringUtils.hasText(hint)) {
      sb.append(" — ").append(hint);
    }
    return sb.toString();
  }

  private static String deepestMessage(Throwable e) {
    if (e == null) {
      return "unknown error";
    }
    Throwable cur = e;
    String last = null;
    int guard = 0;
    while (cur != null && guard++ < 12) {
      if (StringUtils.hasText(cur.getMessage())) {
        last = cur.getMessage();
      }
      cur = cur.getCause();
    }
    return StringUtils.hasText(last) ? last : e.getClass().getSimpleName();
  }

  private static String smtpAuthHint(String host, String serverMessage) {
    if (!StringUtils.hasText(serverMessage)) {
      return "";
    }
    String m = serverMessage.toLowerCase();
    boolean authLike = m.contains("550") || m.contains("no permission") || m.contains("authentication failed");
    if (!authLike) {
      return "";
    }
    String h = host == null ? "" : host.toLowerCase();
    if (h.contains("163.com") || h.contains("126.com") || h.contains("yeah.net")) {
      return "NetEase mail: enable SMTP in webmail, use the client authorization code as the SMTP password "
          + "(not your web login password), set mailFrom to the same mailbox as SMTP username, "
          + "and use smtp.163.com with port 465+SSL or 587+STARTTLS matching your smtpSsl toggle.";
    }
    return "Check SMTP is enabled for the mailbox, use app/client password if required, "
        + "and keep From address aligned with the authenticated account.";
  }

  public void sendTestSmsFromChannel(IotNotifyChannelEntity ch, String phone) {
    sendSmsFromChannel(ch, phone, "{\"content\":\"Skylark SMS test\"}");
  }

  /**
   * SMS dispatch by {@link IotNotifyChannelEntity#getProvider()}:
   * ALIYUN (SendSms), TWILIO (REST Messages API), AWS_SNS (Publish to phone number).
   */
  public void sendSmsFromChannel(IotNotifyChannelEntity ch, String phone, String templateParamJson) {
    if (!StringUtils.hasText(phone)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "recipient phone required");
    }
    String pv = ch.getProvider() == null ? "" : ch.getProvider().trim().toUpperCase();
    JsonNode cfg = requireJson(ch);
    if ("TWILIO".equals(pv)) {
      sendTwilioSms(cfg, phone.trim(), templateBody(templateParamJson));
      return;
    }
    if ("AWS_SNS".equals(pv)) {
      sendAwsSnsSms(cfg, phone.trim(), templateBody(templateParamJson));
      return;
    }
    if ("ALIYUN".equals(pv) || pv.isEmpty()) {
      sendAliyunSms(ch, phone.trim(), templateParamJson);
      return;
    }
    throw new MgmtException(HttpStatus.BAD_REQUEST, "unsupported sms provider: " + ch.getProvider());
  }

  private static String templateBody(String templateParamJson) {
    if (!StringUtils.hasText(templateParamJson)) {
      return "notify";
    }
    try {
      JsonNode n = new ObjectMapper().readTree(templateParamJson);
      if (n != null && n.has("content")) {
        return n.get("content").asText();
      }
    } catch (Exception ignored) {
      /* fall through */
    }
    return templateParamJson;
  }

  private void sendAliyunSms(IotNotifyChannelEntity ch, String phone, String templateParamJson) throws MgmtException {
    JsonNode cfg = requireJson(ch);
    String accessKeyId = text(cfg, "smsAccessKeyId");
    String accessKeySecret = text(cfg, "smsAccessKeySecret");
    String signName = text(cfg, "smsSignName");
    String templateCode = text(cfg, "smsTemplateCode");
    if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)
        || !StringUtils.hasText(signName) || !StringUtils.hasText(templateCode)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "sms channel not fully configured");
    }
    String region = StringUtils.hasText(text(cfg, "smsRegionId")) ? text(cfg, "smsRegionId").trim() : "cn-hangzhou";
    try {
      String body = aliyunSendSms(accessKeyId.trim(), accessKeySecret.trim(), region,
          phone, signName.trim(), templateCode.trim(), templateParamJson);
      if (body != null && body.contains("\"Code\":\"OK\"")) {
        return;
      }
      log.warn("Aliyun SMS unexpected response: {}", body);
      throw new MgmtException(HttpStatus.BAD_REQUEST, "sms send failed: " + (body != null ? body : "empty response"));
    } catch (MgmtException e) {
      throw e;
    } catch (Exception e) {
      log.warn("sendSms failed phone={}", phone, e);
      throw new MgmtException(HttpStatus.BAD_REQUEST, "sms send failed: " + e.getMessage());
    }
  }

  private static String urlFormEncode(String s) {
    if (s == null) {
      return "";
    }
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (Exception e) {
      return "";
    }
  }

  private static String truncateUtf16(String s, int maxChars) {
    if (s == null) {
      return "";
    }
    if (s.length() <= maxChars) {
      return s;
    }
    return s.substring(0, Math.max(0, maxChars - 1)) + "…";
  }

  private void sendTwilioSms(JsonNode cfg, String toPhone, String messageText) {
    String accountSid = text(cfg, "twilioAccountSid");
    String authToken = text(cfg, "twilioAuthToken");
    String from = text(cfg, "twilioFrom");
    String messagingServiceSid = text(cfg, "twilioMessagingServiceSid");
    if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "twilio channel requires twilioAccountSid and twilioAuthToken");
    }
    if (!StringUtils.hasText(from) && !StringUtils.hasText(messagingServiceSid)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "twilio channel requires twilioFrom or twilioMessagingServiceSid");
    }
    String msg = truncateUtf16(messageText, 1600);
    String urlStr = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid.trim() + "/Messages.json";
    StringBuilder form = new StringBuilder();
    form.append("To=").append(urlFormEncode(toPhone.trim()));
    form.append("&Body=").append(urlFormEncode(msg));
    if (StringUtils.hasText(messagingServiceSid)) {
      form.append("&MessagingServiceSid=").append(urlFormEncode(messagingServiceSid.trim()));
    } else {
      form.append("&From=").append(urlFormEncode(from.trim()));
    }
    byte[] payload = form.toString().getBytes(StandardCharsets.UTF_8);
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
      conn.setRequestMethod("POST");
      conn.setConnectTimeout(15000);
      conn.setReadTimeout(20000);
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      String basic = Base64.getEncoder().encodeToString(
          (accountSid.trim() + ":" + authToken.trim()).getBytes(StandardCharsets.UTF_8));
      conn.setRequestProperty("Authorization", "Basic " + basic);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(payload);
      }
      int code = conn.getResponseCode();
      StringBuilder resp = new StringBuilder();
      java.io.InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
      if (stream != null) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
          String line;
          while ((line = br.readLine()) != null) {
            resp.append(line);
          }
        }
      }
      if (code < 200 || code >= 300) {
        throw new MgmtException(HttpStatus.BAD_REQUEST, "twilio sms http " + code + ": " + resp);
      }
    } catch (MgmtException e) {
      throw e;
    } catch (Exception e) {
      log.warn("twilio sms failed phone={}", toPhone, e);
      throw new MgmtException(HttpStatus.BAD_REQUEST, "twilio sms send failed: " + e.getMessage());
    }
  }

  private void sendAwsSnsSms(JsonNode cfg, String toPhone, String messageText) {
    String ak = text(cfg, "awsAccessKeyId");
    String sk = text(cfg, "awsSecretAccessKey");
    String regionStr = StringUtils.hasText(text(cfg, "awsRegion")) ? text(cfg, "awsRegion").trim() : "us-east-1";
    if (!StringUtils.hasText(ak) || !StringUtils.hasText(sk)) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "aws sns channel requires awsAccessKeyId and awsSecretAccessKey");
    }
    String msg = truncateUtf16(messageText, 1400);
    try (SnsClient client = SnsClient.builder()
        .region(Region.of(regionStr))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ak.trim(), sk.trim())))
        .build()) {
      client.publish(PublishRequest.builder().phoneNumber(toPhone.trim()).message(msg).build());
    } catch (SnsException e) {
      log.warn("aws sns failed phone={}", toPhone, e);
      throw new MgmtException(HttpStatus.BAD_REQUEST,
          "aws sns send failed: " + (e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage()));
    } catch (Exception e) {
      log.warn("aws sns failed phone={}", toPhone, e);
      throw new MgmtException(HttpStatus.BAD_REQUEST, "aws sns send failed: " + e.getMessage());
    }
  }

  private JsonNode requireJson(IotNotifyChannelEntity ch) {
    try {
      if (!StringUtils.hasText(ch.getConfigJson())) {
        return objectMapper.createObjectNode();
      }
      return objectMapper.readTree(ch.getConfigJson());
    } catch (Exception e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "invalid config_json");
    }
  }

  private static String text(JsonNode cfg, String field) {
    if (cfg == null || !cfg.has(field) || cfg.get(field).isNull()) {
      return "";
    }
    return cfg.get(field).asText("");
  }

  private String aliyunSendSms(String accessKeyId, String accessKeySecret, String regionId,
                                 String phoneNumbers, String signName, String templateCode, String templateParam)
      throws Exception {
    TreeMap<String, String> params = new TreeMap<String, String>();
    params.put("SignatureMethod", "HMAC-SHA1");
    params.put("SignatureNonce", String.valueOf(System.currentTimeMillis()));
    params.put("AccessKeyId", accessKeyId);
    params.put("SignatureVersion", "1.0");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    params.put("Timestamp", sdf.format(new Date()));
    params.put("Format", "JSON");
    params.put("Version", "2017-05-25");
    params.put("Action", "SendSms");
    params.put("RegionId", regionId);
    params.put("PhoneNumbers", phoneNumbers);
    params.put("SignName", signName);
    params.put("TemplateCode", templateCode);
    params.put("TemplateParam", templateParam);

    String canonical = buildCanonicalizedQuery(params);
    String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(canonical);
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
    byte[] sign = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
    String signature = Base64.getEncoder().encodeToString(sign);
    params.put("Signature", signature);

    StringBuilder url = new StringBuilder(ALIYUN_SMS_ENDPOINT + "?");
    boolean first = true;
    for (Map.Entry<String, String> e : params.entrySet()) {
      if (!first) {
        url.append('&');
      }
      first = false;
      url.append(percentEncode(e.getKey())).append('=').append(percentEncode(e.getValue()));
    }

    HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(15000);
    StringBuilder resp = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        resp.append(line);
      }
    }
    return resp.toString();
  }

  private static String buildCanonicalizedQuery(TreeMap<String, String> sorted) throws Exception {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> e : sorted.entrySet()) {
      if ("Signature".equals(e.getKey())) {
        continue;
      }
      if (!first) {
        sb.append('&');
      }
      first = false;
      sb.append(percentEncode(e.getKey())).append('=').append(percentEncode(e.getValue()));
    }
    return sb.toString();
  }

  private static String percentEncode(String s) throws Exception {
    if (s == null) {
      return "";
    }
    return URLEncoder.encode(s, "UTF-8")
        .replace("+", "%20")
        .replace("*", "%2A")
        .replace("%7E", "~");
  }

  /** Deep-merge {@code patch} into {@code base} for JSON objects (used on update). */
  public static String mergeConfigJson(String baseJson, String patchJson, ObjectMapper mapper) {
    try {
      JsonNode base = StringUtils.hasText(baseJson) ? mapper.readTree(baseJson) : mapper.createObjectNode();
      if (!(base instanceof ObjectNode)) {
        base = mapper.createObjectNode();
      }
      ObjectNode out = (ObjectNode) base;
      if (!StringUtils.hasText(patchJson)) {
        return mapper.writeValueAsString(out);
      }
      JsonNode patch = mapper.readTree(patchJson);
      if (!(patch instanceof ObjectNode)) {
        return mapper.writeValueAsString(out);
      }
      Iterator<String> it = ((ObjectNode) patch).fieldNames();
      while (it.hasNext()) {
        String k = it.next();
        JsonNode v = patch.get(k);
        if (v != null && v.isTextual() && v.asText("").isEmpty()) {
          continue;
        }
        // API responses mask secrets as "********"; never persist that over real stored values.
        if (shouldSkipMaskedSecretMerge(k, v)) {
          continue;
        }
        out.set(k, v);
      }
      return mapper.writeValueAsString(out);
    } catch (Exception e) {
      throw new MgmtException(HttpStatus.BAD_REQUEST, "invalid config json");
    }
  }

  private static boolean shouldSkipMaskedSecretMerge(String key, JsonNode v) {
    if (v == null || !v.isTextual()) {
      return false;
    }
    if (!"********".equals(v.asText())) {
      return false;
    }
    return "smtpPassword".equals(key) || "smsAccessKeySecret".equals(key) || "authorization".equals(key)
        || "twilioAuthToken".equals(key) || "awsSecretAccessKey".equals(key);
  }

  public static String maskSecretsInConfigJson(String json, ObjectMapper mapper) {
    if (!StringUtils.hasText(json)) {
      return "{}";
    }
    try {
      JsonNode n = mapper.readTree(json);
      if (!(n instanceof ObjectNode)) {
        return json;
      }
      ObjectNode o = (ObjectNode) n.deepCopy();
      maskNode(o, "smtpPassword");
      maskNode(o, "smsAccessKeySecret");
      maskNode(o, "authorization");
      maskNode(o, "twilioAuthToken");
      maskNode(o, "awsSecretAccessKey");
      return mapper.writeValueAsString(o);
    } catch (Exception e) {
      return json;
    }
  }

  private static void maskNode(ObjectNode o, String field) {
    if (o.has(field) && o.get(field).isTextual() && StringUtils.hasText(o.get(field).asText())) {
      o.put(field, "********");
    }
  }
}

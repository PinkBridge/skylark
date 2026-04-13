package cn.skylark.permission.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson配置类 - 全局日期时间格式化
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Configuration
public class JacksonConfig {

  /**
   * 日期时间格式化器：yyyy-MM-dd hh:mm:ss
   */
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

  @Bean
  @Primary
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper objectMapper = builder.build();
    
    // 注册JavaTimeModule以支持Java 8时间类型
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    // 注册自定义的LocalDateTime序列化器和反序列化器
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    
    objectMapper.registerModule(javaTimeModule);
    // 禁用将日期写为时间戳
    objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    return objectMapper;
  }

  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    
    // 注册JavaTimeModule
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    builder.modules(javaTimeModule);
    
    // 注册自定义的 LocalDateTime 序列化器和反序列化器（作为备用）
    SimpleModule module = new SimpleModule();
    module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    builder.modules(module);
    
    // 禁用将日期写为时间戳
    builder.featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    return builder;
  }

  /**
   * LocalDateTime 自定义序列化器
   */
  public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws java.io.IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        gen.writeString(value.format(DATE_TIME_FORMATTER));
      }
    }
  }

  /**
   * LocalDateTime 自定义反序列化器
   * 支持多种日期格式：
   * 1. ISO 8601格式：2025-12-01T01:53:27.000Z
   * 2. 标准格式：2025-12-01 01:53:27
   * 3. 简化格式：2025-12-01T01:53:27
   */
  public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter ISO_WITH_Z = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String value = p.getText();
      if (value == null || value.trim().isEmpty()) {
        return null;
      }

      String trimmed = value.trim();

      try {
        // 尝试解析 ISO 8601 格式（包含时区Z，如：2025-12-01T01:53:27.000Z）
        if (trimmed.endsWith("Z")) {
          try {
            // 先尝试带毫秒的格式
            if (trimmed.contains(".")) {
              ZonedDateTime zonedDateTime = ZonedDateTime.parse(trimmed, DateTimeFormatter.ISO_DATE_TIME);
              return zonedDateTime.toLocalDateTime();
            } else {
              // 不带毫秒的格式
              String withoutZ = trimmed.substring(0, trimmed.length() - 1);
              LocalDateTime localDateTime = LocalDateTime.parse(withoutZ, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
              return localDateTime;
            }
          } catch (DateTimeParseException e) {
            // 如果解析失败，继续尝试其他格式
          }
        }

        // 尝试解析标准格式：yyyy-MM-dd HH:mm:ss
        if (trimmed.contains(" ") && !trimmed.contains("T")) {
          try {
            return LocalDateTime.parse(trimmed, STANDARD_FORMATTER);
          } catch (DateTimeParseException e) {
            // 如果解析失败，继续尝试其他格式
          }
        }

        // 尝试解析简化格式：yyyy-MM-ddTHH:mm:ss
        if (trimmed.contains("T") && !trimmed.endsWith("Z") && !trimmed.contains("+")) {
          int tIndex = trimmed.indexOf("T");
          // 检查T之后是否还有"-"（用于排除时区格式，如+08:00或-05:00）
          if (tIndex > 0 && trimmed.indexOf("-", tIndex) < 0 && trimmed.indexOf("+", tIndex) < 0) {
            try {
              return LocalDateTime.parse(trimmed, SIMPLE_FORMATTER);
            } catch (DateTimeParseException e) {
              // 如果解析失败，继续尝试其他格式
            }
          }
        }

        // 默认尝试 ISO 格式
        try {
          return LocalDateTime.parse(trimmed, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
          // 如果还是失败，尝试带时区的格式
          try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(trimmed);
            return zonedDateTime.toLocalDateTime();
          } catch (DateTimeParseException ex) {
            throw new IOException("无法解析时间格式: " + value, ex);
          }
        }
      } catch (DateTimeParseException e) {
        throw new IOException("无法解析时间格式: " + value, e);
      }
    }
  }
}


package cn.skylark.permission.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime 转换器
 * 支持多种时间格式：
 * 1. ISO 8601格式：2025-11-22T16:00:00.000Z
 * 2. 标准格式：2025-11-22 16:00:00
 * 3. 简化格式：2025-11-22T16:00:00
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Component
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public LocalDateTime convert(String source) {
    if (source == null || source.trim().isEmpty()) {
      return null;
    }

    String trimmed = source.trim();

    try {
      // 尝试解析 ISO 8601 格式（包含时区，如：2025-11-22T16:00:00.000Z）
      if (trimmed.endsWith("Z") || trimmed.contains("+") || (trimmed.contains("-") && trimmed.length() > 19 && trimmed.indexOf("T") > 0)) {
        try {
          ZonedDateTime zonedDateTime = ZonedDateTime.parse(trimmed, ISO_FORMATTER);
          return zonedDateTime.toLocalDateTime();
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
        try {
          return LocalDateTime.parse(trimmed, SIMPLE_FORMATTER);
        } catch (DateTimeParseException e) {
          // 如果解析失败，继续尝试其他格式
        }
      }

      // 默认尝试 ISO 格式
      return LocalDateTime.parse(trimmed, ISO_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("无法解析时间格式: " + source, e);
    }
  }
}


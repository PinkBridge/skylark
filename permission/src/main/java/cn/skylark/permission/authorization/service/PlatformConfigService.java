package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.PlatformConfigItemDTO;
import cn.skylark.permission.authorization.entity.SysPlatformConfig;
import cn.skylark.permission.authorization.mapper.PlatformConfigMapper;
import cn.skylark.permission.authorization.support.PlatformConfigKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 平台级参数：启动加载 + 定时从库刷新；更新后立即重载缓存。
 */
@Slf4j
@Service
@DependsOn("flywayInitializer")
public class PlatformConfigService {

  private static final String TYPE_BOOL = "BOOL";

  private final Map<String, String> cache = new ConcurrentHashMap<>();

  @Resource
  private PlatformConfigMapper platformConfigMapper;

  @PostConstruct
  public void init() {
    reloadFromDatabase();
  }

  @Scheduled(
      fixedDelayString = "${platform.config.refresh-ms:60000}",
      initialDelayString = "${platform.config.refresh-ms:60000}"
  )
  public void scheduledReload() {
    reloadFromDatabase();
  }

  public void reloadFromDatabase() {
    try {
      List<SysPlatformConfig> rows = platformConfigMapper.selectAll();
      Map<String, String> next = new ConcurrentHashMap<>();
      for (SysPlatformConfig row : rows) {
        if (row.getConfigKey() != null) {
          next.put(row.getConfigKey(), row.getConfigValue() != null ? row.getConfigValue() : "");
        }
      }
      cache.clear();
      cache.putAll(next);
      log.debug("Platform config cache reloaded, size={}", cache.size());
    } catch (Exception e) {
      log.warn("Platform config reload failed, keep previous cache", e);
    }
  }

  /**
   * 为 true 时操作日志仅记录写方法。
   */
  public boolean isOperationLogWritesOnly() {
    return parseBoolean(cache.get(PlatformConfigKeys.OPERATION_LOG_WRITES_ONLY));
  }

  /**
   * 租户域名变更时，需要同步 redirect_uri 的 OAuth clientId 列表（逗号分隔）。
   */
  public List<String> tenantDomainSyncClientIds() {
    return parseCsvList(cache.get(PlatformConfigKeys.TENANT_DOMAIN_SYNC_CLIENT_IDS));
  }

  public List<PlatformConfigItemDTO> listAll() {
    List<SysPlatformConfig> rows = platformConfigMapper.selectAll();
    if (rows == null || rows.isEmpty()) {
      return Collections.emptyList();
    }
    return rows.stream().map(this::toItem).collect(Collectors.toList());
  }

  public int updateValue(String configKey, String newValue) {
    if (!StringUtils.hasText(configKey)) {
      return 0;
    }
    SysPlatformConfig existing = platformConfigMapper.selectByKey(configKey);
    if (existing == null) {
      return 0;
    }
    String normalized = normalizeValue(existing.getValueType(), newValue);
    int n = platformConfigMapper.updateValueByKey(configKey, normalized);
    if (n > 0) {
      reloadFromDatabase();
    }
    return n;
  }

  private PlatformConfigItemDTO toItem(SysPlatformConfig row) {
    PlatformConfigItemDTO dto = new PlatformConfigItemDTO();
    BeanUtils.copyProperties(row, dto);
    return dto;
  }

  private static String normalizeValue(String valueType, String raw) {
    if (!StringUtils.hasText(valueType)) {
      return raw != null ? raw : "";
    }
    if (TYPE_BOOL.equalsIgnoreCase(valueType)) {
      if (!StringUtils.hasText(raw)) {
        return "false";
      }
      boolean b = parseBoolean(raw.trim());
      return b ? "true" : "false";
    }
    return raw != null ? raw : "";
  }

  private static boolean parseBoolean(String raw) {
    if (!StringUtils.hasText(raw)) {
      return false;
    }
    String s = raw.trim().toLowerCase(Locale.ROOT);
    return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "on".equals(s);
  }

  private static List<String> parseCsvList(String raw) {
    if (!StringUtils.hasText(raw)) {
      return Collections.emptyList();
    }
    String[] parts = raw.split(",");
    List<String> out = new ArrayList<>();
    for (String p : parts) {
      if (!StringUtils.hasText(p)) {
        continue;
      }
      String v = p.trim();
      if (StringUtils.hasText(v)) {
        out.add(v);
      }
    }
    return out;
  }
}

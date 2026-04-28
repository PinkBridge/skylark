package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.importing.ApiImportFile;
import cn.skylark.permission.authorization.dto.importing.ApiImportItem;
import cn.skylark.permission.authorization.dto.importing.ImportSummary;
import cn.skylark.permission.authorization.entity.SysApi;
import cn.skylark.permission.authorization.mapper.ApiMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ApiImportService {

  private final ApiMapper apiMapper;

  public ApiImportService(ApiMapper apiMapper) {
    this.apiMapper = apiMapper;
  }

  @Transactional
  public ImportSummary importApis(String appCode, ApiImportFile file, boolean dryRun) {
    ImportSummary summary = new ImportSummary();
    summary.setDryRun(dryRun);

    if (!StringUtils.hasText(appCode)) {
      throw new IllegalArgumentException("appCode.required");
    }
    if (file == null || file.getApis() == null) {
      throw new IllegalArgumentException("import.file.invalid");
    }

    List<ApiImportItem> items = file.getApis();
    Set<String> seen = new HashSet<>();
    for (ApiImportItem item : items) {
      if (item == null) {
        continue;
      }
      String method = normalizeMethod(item.getMethod());
      String path = item.getPath();
      if (!StringUtils.hasText(method) || !StringUtils.hasText(path) || !StringUtils.hasText(item.getPermlabel())) {
        throw new IllegalArgumentException("api.item.invalid");
      }
      String key = method + ":" + path;
      if (!seen.add(key)) {
        summary.getWarnings().add("duplicated.api: " + key);
      }

      SysApi existing = apiMapper.selectActiveByMethodAndPath(method, path);
      if (existing != null) {
        summary.setUpdated(summary.getUpdated() + 1);
        if (!dryRun) {
          existing.setPermlabel(item.getPermlabel());
          existing.setModuleKey(item.getModuleKey());
          existing.setAppCode(appCode);
          apiMapper.update(existing);
        }
        continue;
      }

      SysApi deleted = apiMapper.selectDeletedByMethodAndPath(method, path);
      if (deleted != null) {
        summary.setReactivated(summary.getReactivated() + 1);
        if (!dryRun) {
          apiMapper.reactivateById(deleted.getId());
          deleted.setPermlabel(item.getPermlabel());
          deleted.setModuleKey(item.getModuleKey());
          deleted.setAppCode(appCode);
          apiMapper.update(deleted);
        }
        continue;
      }

      summary.setCreated(summary.getCreated() + 1);
      if (!dryRun) {
        SysApi row = new SysApi();
        row.setMethod(method);
        row.setPath(path);
        row.setPermlabel(item.getPermlabel());
        row.setModuleKey(item.getModuleKey());
        row.setAppCode(appCode);
        apiMapper.insert(row);
      }
    }

    return summary;
  }

  private static String normalizeMethod(String method) {
    if (!StringUtils.hasText(method)) {
      return null;
    }
    return method.trim().toUpperCase(Locale.ROOT);
  }
}


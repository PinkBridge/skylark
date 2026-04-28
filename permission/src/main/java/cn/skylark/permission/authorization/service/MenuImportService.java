package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.importing.ImportSummary;
import cn.skylark.permission.authorization.dto.importing.MenuImportFile;
import cn.skylark.permission.authorization.dto.importing.MenuImportItem;
import cn.skylark.permission.authorization.entity.SysMenu;
import cn.skylark.permission.authorization.mapper.MenuMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class MenuImportService {

  private final MenuMapper menuMapper;
  private final ObjectMapper objectMapper;

  public MenuImportService(MenuMapper menuMapper, ObjectMapper objectMapper) {
    this.menuMapper = menuMapper;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public ImportSummary importMenus(String appCode, MenuImportFile file, boolean dryRun) {
    ImportSummary summary = new ImportSummary();
    summary.setDryRun(dryRun);

    if (!StringUtils.hasText(appCode)) {
      throw new IllegalArgumentException("appCode.required");
    }
    if (file == null || file.getMenus() == null) {
      throw new IllegalArgumentException("import.file.invalid");
    }

    List<MenuImportItem> items = file.getMenus();
    Map<String, MenuImportItem> byPerm = new LinkedHashMap<>();
    for (MenuImportItem item : items) {
      if (item == null) {
        continue;
      }
      if (!StringUtils.hasText(item.getPermlabel())) {
        throw new IllegalArgumentException("menu.permlabel.required");
      }
      if (byPerm.containsKey(item.getPermlabel())) {
        summary.getWarnings().add("duplicated.menu.permlabel: " + item.getPermlabel());
      }
      byPerm.put(item.getPermlabel(), item);
    }

    Map<String, Long> resolvedIds = new HashMap<>();
    // preload existing parents (best-effort)
    for (MenuImportItem item : byPerm.values()) {
      if (item == null || !StringUtils.hasText(item.getParentPermlabel())) {
        continue;
      }
      String p = item.getParentPermlabel();
      if (!resolvedIds.containsKey(p)) {
        SysMenu existingParent = menuMapper.selectActiveByAppCodeAndPermlabel(appCode, p);
        if (existingParent != null) {
          resolvedIds.put(p, existingParent.getId());
        }
      }
    }

    Set<String> remaining = new LinkedHashSet<>(byPerm.keySet());
    int guard = 0;
    while (!remaining.isEmpty()) {
      if (++guard > byPerm.size() + 5) {
        throw new IllegalArgumentException("menu.parent.cycle.or.missing");
      }

      boolean progressed = false;
      Iterator<String> it = remaining.iterator();
      while (it.hasNext()) {
        String permlabel = it.next();
        MenuImportItem item = byPerm.get(permlabel);
        if (item == null) {
          it.remove();
          continue;
        }

        Long parentId = null;
        if (StringUtils.hasText(item.getParentPermlabel())) {
          parentId = resolvedIds.get(item.getParentPermlabel());
          if (parentId == null) {
            // parent might be in payload, but not processed yet
            continue;
          }
        }

        SysMenu existing = menuMapper.selectActiveByAppCodeAndPermlabel(appCode, permlabel);
        if (existing != null) {
          summary.setUpdated(summary.getUpdated() + 1);
          if (!dryRun) {
            applyToRow(appCode, parentId, item, existing);
            menuMapper.update(existing);
          }
          resolvedIds.put(permlabel, existing.getId());
          it.remove();
          progressed = true;
          continue;
        }

        SysMenu deleted = menuMapper.selectDeletedByAppCodeAndPermlabel(appCode, permlabel);
        if (deleted != null) {
          summary.setReactivated(summary.getReactivated() + 1);
          if (!dryRun) {
            menuMapper.reactivateById(deleted.getId());
            applyToRow(appCode, parentId, item, deleted);
            menuMapper.update(deleted);
          }
          resolvedIds.put(permlabel, deleted.getId());
          it.remove();
          progressed = true;
          continue;
        }

        summary.setCreated(summary.getCreated() + 1);
        if (!dryRun) {
          SysMenu row = new SysMenu();
          applyToRow(appCode, parentId, item, row);
          menuMapper.insert(row);
          resolvedIds.put(permlabel, row.getId());
        } else {
          // dryRun: still mark as resolved so that children can be validated/processed
          resolvedIds.put(permlabel, -1L);
        }
        it.remove();
        progressed = true;
      }

      if (!progressed) {
        throw new IllegalArgumentException("menu.parent.missing: " + remaining.iterator().next());
      }
    }

    return summary;
  }

  private void applyToRow(String appCode, Long parentId, MenuImportItem item, SysMenu row) {
    row.setAppCode(appCode);
    row.setParentId(parentId);
    row.setPermlabel(item.getPermlabel());
    row.setType(StringUtils.hasText(item.getType()) ? item.getType() : "menu");
    row.setName(StringUtils.hasText(item.getName()) ? item.getName() : item.getPermlabel());
    row.setNameI18n(toNameI18nJson(item.getNameI18n()));
    row.setPath(item.getPath());
    row.setIcon(item.getIcon());
    row.setSort(item.getSort() == null ? 0 : item.getSort());
    row.setHidden(item.getHidden() != null && item.getHidden());
    row.setModuleKey(item.getModuleKey());
  }

  private String toNameI18nJson(Map<String, String> nameI18n) {
    if (nameI18n == null || nameI18n.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(nameI18n);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("menu.nameI18n.invalid");
    }
  }
}


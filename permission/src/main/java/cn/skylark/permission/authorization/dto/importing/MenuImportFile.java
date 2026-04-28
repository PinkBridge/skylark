package cn.skylark.permission.authorization.dto.importing;

import lombok.Data;

import java.util.List;

@Data
public class MenuImportFile {
  private Integer schemaVersion;
  private String source;
  private List<MenuImportItem> menus;
}


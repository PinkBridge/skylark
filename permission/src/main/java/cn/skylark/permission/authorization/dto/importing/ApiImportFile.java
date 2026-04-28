package cn.skylark.permission.authorization.dto.importing;

import lombok.Data;

import java.util.List;

@Data
public class ApiImportFile {
  private Integer schemaVersion;
  private String source;
  private List<ApiImportItem> apis;
}


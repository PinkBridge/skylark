package cn.skylark.permission.authorization.dto.importing;

import lombok.Data;

@Data
public class ApiImportItem {
  private String method;
  private String path;
  private String permlabel;
  private String moduleKey;
}


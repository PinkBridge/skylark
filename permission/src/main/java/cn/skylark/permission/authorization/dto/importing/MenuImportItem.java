package cn.skylark.permission.authorization.dto.importing;

import lombok.Data;

import java.util.Map;

@Data
public class MenuImportItem {
  private String permlabel;
  private String parentPermlabel;
  private String type;
  private String name;
  private Map<String, String> nameI18n;
  private String path;
  private String icon;
  private Integer sort;
  private Boolean hidden;
  private String moduleKey;
}


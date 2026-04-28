package cn.skylark.permission.authorization.dto.importing;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportSummary {
  private boolean dryRun;
  private int created;
  private int updated;
  private int reactivated;
  private List<String> warnings = new ArrayList<>();
}


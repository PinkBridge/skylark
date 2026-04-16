package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlatformInitStateDTO {
  private boolean initialized;
  private String status;
  private LocalDateTime finishedAt;
}


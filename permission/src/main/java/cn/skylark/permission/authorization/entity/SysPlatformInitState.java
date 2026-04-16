package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPlatformInitState {
  private Long id;
  private String status; // PENDING | COMPLETED
  private LocalDateTime finishedAt;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}


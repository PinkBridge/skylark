package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysOauthClientMeta {
  private String clientId;
  private String name;
  private Integer port;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}


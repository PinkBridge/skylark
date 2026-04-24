package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysOauthClientMeta {
  private String clientId;
  private String name;
  private String logo;
  private Integer port;
  private Boolean show;
  private Integer sort;
  private String terminalType;
  private Boolean open;
  private Boolean isDelete;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}


package cn.skylark.business_service.blog.model;

import lombok.Data;

import java.util.Date;

@Data
public class BlogPost {
  private Long id;
  private String tenantId;
  private Long orgId;
  private String title;
  private String summary;
  private String content;
  private String status;
  private String tags;
  private Boolean isDelete;
  private String createUser;
  private String updateUser;
  private Date createTime;
  private Date updateTime;
}


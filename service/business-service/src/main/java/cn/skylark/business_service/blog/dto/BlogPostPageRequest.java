package cn.skylark.business_service.blog.dto;

import lombok.Data;

@Data
public class BlogPostPageRequest {
  private Integer page = 1;
  private Integer size = 10;
  private String keyword;
  private String status;
}


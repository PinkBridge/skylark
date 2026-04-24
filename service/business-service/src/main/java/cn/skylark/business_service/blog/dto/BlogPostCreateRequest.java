package cn.skylark.business_service.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class BlogPostCreateRequest {
  @NotBlank
  @Size(max = 200)
  private String title;

  @Size(max = 500)
  private String summary;

  @NotBlank
  private String content;

  @NotBlank
  private String status; // DRAFT/PUBLISHED/ARCHIVED

  @Size(max = 500)
  private String tags; // comma-separated
}


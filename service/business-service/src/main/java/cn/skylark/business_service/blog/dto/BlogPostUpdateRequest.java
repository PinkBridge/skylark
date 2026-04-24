package cn.skylark.business_service.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class BlogPostUpdateRequest {
  @NotBlank
  @Size(max = 200)
  private String title;

  @Size(max = 500)
  private String summary;

  @NotBlank
  private String content;

  @NotBlank
  private String status;

  @Size(max = 500)
  private String tags;
}


package cn.skylark.business_service.blog.web;

import cn.skylark.business_service.blog.dto.BlogPostCreateRequest;
import cn.skylark.business_service.blog.dto.BlogPostPageRequest;
import cn.skylark.business_service.blog.dto.BlogPostUpdateRequest;
import cn.skylark.business_service.blog.model.BlogPost;
import cn.skylark.business_service.blog.service.BlogPostService;
import cn.skylark.web.common.PageResult;
import cn.skylark.web.common.Ret;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/business-service/blog/posts")
public class BlogPostController {

  @Resource
  private BlogPostService blogPostService;

  @PostMapping
  public Ret<BlogPost> create(@RequestHeader("X-Tenant-Id") String tenantId,
                              @Valid @RequestBody BlogPostCreateRequest req) {
    return Ret.data(blogPostService.create(tenantId, req));
  }

  @PutMapping("/{id}")
  public Ret<BlogPost> update(@RequestHeader("X-Tenant-Id") String tenantId,
                              @PathVariable Long id,
                              @Valid @RequestBody BlogPostUpdateRequest req) {
    BlogPost updated = blogPostService.update(tenantId, id, req);
    if (updated == null) {
      return Ret.fail(404, "blog.post.not.found");
    }
    return Ret.data(updated);
  }

  @DeleteMapping("/{id}")
  public Ret<Object> delete(@RequestHeader("X-Tenant-Id") String tenantId, @PathVariable Long id) {
    boolean ok = blogPostService.delete(tenantId, id);
    if (!ok) {
      return Ret.fail(404, "blog.post.not.found");
    }
    return Ret.ok();
  }

  @GetMapping("/{id}")
  public Ret<BlogPost> get(@RequestHeader("X-Tenant-Id") String tenantId, @PathVariable Long id) {
    BlogPost post = blogPostService.get(tenantId, id);
    if (post == null) {
      return Ret.fail(404, "blog.post.not.found");
    }
    return Ret.data(post);
  }

  @GetMapping("/page")
  public Ret<PageResult<BlogPost>> page(@RequestHeader("X-Tenant-Id") String tenantId,
                                        @RequestParam(required = false, defaultValue = "1") Integer page,
                                        @RequestParam(required = false, defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String status) {
    BlogPostPageRequest req = new BlogPostPageRequest();
    req.setPage(page);
    req.setSize(size);
    req.setKeyword(keyword);
    req.setStatus(status);
    return Ret.data(blogPostService.page(tenantId, req));
  }
}


package cn.skylark.business_service.blog.service;

import cn.skylark.business_service.blog.dto.BlogPostCreateRequest;
import cn.skylark.business_service.blog.dto.BlogPostPageRequest;
import cn.skylark.business_service.blog.dto.BlogPostUpdateRequest;
import cn.skylark.business_service.blog.mapper.BlogPostMapper;
import cn.skylark.business_service.blog.model.BlogPost;
import cn.skylark.web.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BlogPostService {

  @Resource
  private BlogPostMapper blogPostMapper;

  public BlogPost create(String tenantId, BlogPostCreateRequest req) {
    BlogPost post = new BlogPost();
    post.setTenantId(tenantId);
    post.setTitle(req.getTitle());
    post.setSummary(req.getSummary());
    post.setContent(req.getContent());
    post.setStatus(req.getStatus());
    post.setTags(req.getTags());
    blogPostMapper.insert(post);
    return blogPostMapper.findByIdAndTenant(post.getId(), tenantId);
  }

  public BlogPost update(String tenantId, Long id, BlogPostUpdateRequest req) {
    BlogPost post = new BlogPost();
    post.setTitle(req.getTitle());
    post.setSummary(req.getSummary());
    post.setContent(req.getContent());
    post.setStatus(req.getStatus());
    post.setTags(req.getTags());
    int updated = blogPostMapper.updateByIdAndTenant(id, tenantId, post);
    if (updated <= 0) {
      return null;
    }
    return blogPostMapper.findByIdAndTenant(id, tenantId);
  }

  public boolean delete(String tenantId, Long id) {
    return blogPostMapper.softDeleteByIdAndTenant(id, tenantId) > 0;
  }

  public BlogPost get(String tenantId, Long id) {
    return blogPostMapper.findByIdAndTenant(id, tenantId);
  }

  public PageResult<BlogPost> page(String tenantId, BlogPostPageRequest req) {
    int page = (req.getPage() == null || req.getPage() < 1) ? 1 : req.getPage();
    int size = (req.getSize() == null || req.getSize() < 1) ? 10 : Math.min(req.getSize(), 100);
    String keyword = StringUtils.hasText(req.getKeyword()) ? req.getKeyword().trim() : null;
    String status = StringUtils.hasText(req.getStatus()) ? req.getStatus().trim() : null;

    long total = blogPostMapper.countPage(tenantId, keyword, status);
    int offset = (page - 1) * size;
    List<BlogPost> records = total == 0 ? java.util.Collections.emptyList() :
        blogPostMapper.selectPage(tenantId, keyword, status, offset, size);
    return new PageResult<>(records, total, page, size);
  }
}


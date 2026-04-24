package cn.skylark.business_service.blog.mapper;

import cn.skylark.business_service.blog.model.BlogPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogPostMapper {
  int insert(BlogPost post);

  int updateByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId, @Param("post") BlogPost post);

  int softDeleteByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

  BlogPost findByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

  long countPage(@Param("tenantId") String tenantId,
                 @Param("keyword") String keyword,
                 @Param("status") String status);

  List<BlogPost> selectPage(@Param("tenantId") String tenantId,
                            @Param("keyword") String keyword,
                            @Param("status") String status,
                            @Param("offset") int offset,
                            @Param("size") int size);
}


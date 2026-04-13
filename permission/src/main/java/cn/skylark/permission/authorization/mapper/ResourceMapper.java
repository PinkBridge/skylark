package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.dto.UpdateResourceDTO;
import cn.skylark.permission.authorization.entity.SysResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源 Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface ResourceMapper {

  /**
   * 根据ID查询
   */
  SysResource selectById(@Param("id") Long id);

  /**
   * 查询所有
   */
  List<SysResource> selectAll();

  /**
   * 插入
   */
  int insert(SysResource resource);

  /**
   * 更新
   */
  int update(SysResource resource);

  /**
   * 根据ID删除
   */
  int deleteById(@Param("id") Long id);

  /**
   * 分页查询
   */
  List<SysResource> selectPage(@Param("offset") Integer offset,
                               @Param("limit") Integer limit);

  /**
   * 带条件分页查询
   */
  List<SysResource> selectPageWithCondition(@Param("name") String name,
                                            @Param("originalName") String originalName,
                                            @Param("fileType") String fileType,
                                            @Param("createTime") LocalDateTime createTime,
                                            @Param("offset") Integer offset,
                                            @Param("limit") Integer limit);

  /**
   * 统计总数
   */
  Long countAll();

  /**
   * 带条件统计总数
   */
  Long countWithCondition(@Param("name") String name,
                          @Param("originalName") String originalName,
                          @Param("fileType") String fileType,
                          @Param("createTime") LocalDateTime createTime);

  /**
   * 更新资源信息
   */
  int updateResourceInfo(@Param("resourceId") Long resourceId,
                         @Param("resource") UpdateResourceDTO resource);
}


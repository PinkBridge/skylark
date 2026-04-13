package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.dto.UpdateOrganizationDTO;
import cn.skylark.permission.authorization.entity.SysOrganization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 组织 Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface OrganizationMapper {

  /**
   * 根据ID查询
   */
  SysOrganization selectById(@Param("id") Long id);

  /**
   * 根据编码查询
   */
  SysOrganization selectByCode(@Param("code") String code);

  /**
   * 查询所有
   */
  List<SysOrganization> selectAll();

  /**
   * 新增
   */
  int insert(SysOrganization organization);

  /**
   * 更新
   */
  int update(SysOrganization organization);

  /**
   * 删除
   */
  int deleteById(@Param("id") Long id);

  /**
   * 分页查询组织列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 组织列表
   */
  List<SysOrganization> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询组织列表（带条件）
   *
   * @param name      组织名称（模糊搜索）
   * @param code      组织编码（模糊搜索）
   * @param phone     联系电话（模糊搜索）
   * @param email     邮箱（模糊搜索）
   * @param type      组织类型（精确搜索）
   * @param status    状态（精确搜索）
   * @param createTime 创建时间（查询此时间之前的数据）
   * @param offset    偏移量
   * @param limit     限制数量
   * @return 组织列表
   */
  List<SysOrganization> selectPageWithCondition(@Param("name") String name,
                                                 @Param("code") String code,
                                                 @Param("phone") String phone,
                                                 @Param("email") String email,
                                                 @Param("type") String type,
                                                 @Param("status") String status,
                                                 @Param("createTime") java.time.LocalDateTime createTime,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

  /**
   * 统计组织总数
   *
   * @return 组织总数
   */
  Long countAll();

  /**
   * 统计组织总数（带条件）
   *
   * @param name      组织名称（模糊搜索）
   * @param code      组织编码（模糊搜索）
   * @param phone     联系电话（模糊搜索）
   * @param email     邮箱（模糊搜索）
   * @param type      组织类型（精确搜索）
   * @param status    状态（精确搜索）
   * @param createTime 创建时间（查询此时间之前的数据）
   * @return 组织总数
   */
  Long countWithCondition(@Param("name") String name,
                           @Param("code") String code,
                           @Param("phone") String phone,
                           @Param("email") String email,
                           @Param("type") String type,
                           @Param("status") String status,
                           @Param("createTime") java.time.LocalDateTime createTime);

  /**
   * 更新组织信息
   *
   * @param organizationId 组织ID
   * @param organization   组织信息
   * @return 更新行数
   */
  int updateOrganizationInfo(@Param("organizationId") Long organizationId, @Param("organization") UpdateOrganizationDTO organization);
}


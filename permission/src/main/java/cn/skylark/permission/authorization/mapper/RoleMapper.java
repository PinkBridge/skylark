package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
  SysRole selectById(@Param("id") Long id);

  List<SysRole> selectAll();

  int insert(SysRole role);

  int update(SysRole role);

  int deleteById(@Param("id") Long id);

  /**
   * 分页查询角色列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 角色列表
   */
  List<SysRole> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询角色列表（带条件）
   *
   * @param name      角色名称（模糊搜索）
   * @param remark    备注（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据）
   * @param offset    偏移量
   * @param limit     限制数量
   * @return 角色列表
   */
  List<SysRole> selectPageWithCondition(@Param("name") String name,
                                         @Param("remark") String remark,
                                         @Param("createTime") java.time.LocalDateTime createTime,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

  /**
   * 统计角色总数
   *
   * @return 角色总数
   */
  Long countAll();

  /**
   * 统计角色总数（带条件）
   *
   * @param name      角色名称（模糊搜索）
   * @param remark    备注（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据）
   * @return 角色总数
   */
  Long countWithCondition(@Param("name") String name,
                           @Param("remark") String remark,
                           @Param("createTime") java.time.LocalDateTime createTime);

  /**
   * 更新角色信息
   *
   * @param roleId 角色ID
   * @param role   角色信息
   * @return 更新行数
   */
  int updateRoleInfo(@Param("roleId") Long roleId, @Param("role") cn.skylark.permission.authorization.dto.UpdateRoleDTO role);

  /**
   * 按名称和租户ID查询角色
   */
  SysRole selectByNameAndTenantId(@Param("name") String name, @Param("tenantId") Long tenantId);

  /**
   * 查询某个租户下的所有角色
   */
  List<SysRole> selectByTenantId(@Param("tenantId") Long tenantId);
}

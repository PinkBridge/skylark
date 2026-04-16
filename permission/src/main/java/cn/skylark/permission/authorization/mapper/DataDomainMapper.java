package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysDataDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据域Mapper
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Mapper
public interface DataDomainMapper {

  /**
   * 根据ID查询数据域
   *
   * @param id 数据域ID
   * @return 数据域
   */
  SysDataDomain selectById(@Param("id") Long id);

  /**
   * 查询所有数据域
   *
   * @return 数据域列表
   */
  List<SysDataDomain> selectAll();

  /**
   * 插入数据域
   *
   * @param dataDomain 数据域
   * @return 插入行数
   */
  int insert(SysDataDomain dataDomain);

  /**
   * 按 (code, tenantId) 查询数据域（用于生成唯一编码）。
   */
  SysDataDomain selectByCodeAndTenantId(@Param("code") String code, @Param("tenantId") Long tenantId);

  /**
   * 更新数据域
   *
   * @param dataDomain 数据域
   * @return 更新行数
   */
  int update(SysDataDomain dataDomain);

  /**
   * 根据ID删除数据域
   *
   * @param id 数据域ID
   * @return 删除行数
   */
  int deleteById(@Param("id") Long id);

  /**
   * 分页查询数据域列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 数据域列表
   */
  List<SysDataDomain> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询数据域列表（带条件）
   *
   * @param name       数据域名称（模糊搜索）
   * @param code       数据域编码（模糊搜索）
   * @param type       数据范围类型
   * @param enabled    是否启用
   * @param createTime 创建时间（查询此时间之前的数据）
   * @param offset     偏移量
   * @param limit      限制数量
   * @return 数据域列表
   */
  List<SysDataDomain> selectPageWithCondition(@Param("name") String name,
                                               @Param("code") String code,
                                               @Param("type") String type,
                                               @Param("enabled") Boolean enabled,
                                               @Param("createTime") java.time.LocalDateTime createTime,
                                               @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);

  /**
   * 统计数据域总数
   *
   * @return 数据域总数
   */
  Long countAll();

  /**
   * 统计数据域总数（带条件）
   *
   * @param name       数据域名称（模糊搜索）
   * @param code       数据域编码（模糊搜索）
   * @param type       数据范围类型
   * @param enabled    是否启用
   * @param createTime 创建时间（查询此时间之前的数据）
   * @return 数据域总数
   */
  Long countWithCondition(@Param("name") String name,
                          @Param("code") String code,
                          @Param("type") String type,
                          @Param("enabled") Boolean enabled,
                          @Param("createTime") java.time.LocalDateTime createTime);

  /**
   * 更新数据域信息
   *
   * @param dataDomainId 数据域ID
   * @param dataDomain   数据域信息
   * @return 更新行数
   */
  int updateDataDomainInfo(@Param("dataDomainId") Long dataDomainId, @Param("dataDomain") cn.skylark.permission.authorization.dto.UpdateDataDomainDTO dataDomain);

  /**
   * 根据角色ID查询数据域列表
   *
   * @param roleId 角色ID
   * @return 数据域列表
   */
  List<SysDataDomain> selectByRoleId(@Param("roleId") Long roleId);

  /**
   * 删除角色和数据域的所有关联
   *
   * @param roleId 角色ID
   * @return 删除行数
   */
  int deleteBindingsByRoleId(@Param("roleId") Long roleId);

  /**
   * 绑定角色和数据域
   *
   * @param roleId        角色ID
   * @param dataDomainIds 数据域ID列表
   * @return 插入行数
   */
  int bindRoleDataDomains(@Param("roleId") Long roleId, @Param("dataDomainIds") java.util.List<Long> dataDomainIds);
}


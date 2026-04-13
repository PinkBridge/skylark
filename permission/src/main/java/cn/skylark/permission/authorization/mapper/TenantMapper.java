package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.dto.UpdateTenantDTO;
import cn.skylark.permission.authorization.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户 Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface TenantMapper {

  /**
   * 根据ID查询
   */
  SysTenant selectById(@Param("id") Long id);

  /**
   * 根据编码查询
   */
  SysTenant selectByCode(@Param("code") String code);

  /**
   * 根据域名查询
   */
  SysTenant selectByDomain(@Param("domain") String domain);

  /**
   * 根据域名和排除ID查询（用于唯一性校验）
   */
  SysTenant selectByDomainExcludeId(@Param("domain") String domain, @Param("excludeId") Long excludeId);

  /**
   * 查询所有
   */
  List<SysTenant> selectAll();

  /**
   * 新增
   */
  int insert(SysTenant tenant);

  /**
   * 更新
   */
  int update(SysTenant tenant);

  /**
   * 删除
   */
  int deleteById(@Param("id") Long id);

  /**
   * 分页查询租户列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 租户列表
   */
  List<SysTenant> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询租户列表（带条件）
   *
   * @param name         租户名称（模糊搜索）
   * @param code         租户编码（模糊搜索）
   * @param contactPhone 联系人电话（模糊搜索）
   * @param contactEmail 联系人邮箱（模糊搜索）
   * @param domain       租户域名（模糊搜索）
   * @param status       状态（精确搜索）
   * @param createTime   创建时间（查询此时间之前的数据）
   * @param offset       偏移量
   * @param limit        限制数量
   * @return 租户列表
   */
  List<SysTenant> selectPageWithCondition(@Param("name") String name,
                                          @Param("code") String code,
                                          @Param("contactPhone") String contactPhone,
                                          @Param("contactEmail") String contactEmail,
                                          @Param("domain") String domain,
                                          @Param("status") String status,
                                          @Param("createTime") LocalDateTime createTime,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

  /**
   * 统计租户总数
   *
   * @return 租户总数
   */
  Long countAll();

  /**
   * 统计租户总数（带条件）
   *
   * @param name         租户名称（模糊搜索）
   * @param code         租户编码（模糊搜索）
   * @param contactPhone 联系人电话（模糊搜索）
   * @param contactEmail 联系人邮箱（模糊搜索）
   * @param domain       租户域名（模糊搜索）
   * @param status       状态（精确搜索）
   * @param createTime   创建时间（查询此时间之前的数据）
   * @return 租户总数
   */
  Long countWithCondition(@Param("name") String name,
                          @Param("code") String code,
                          @Param("contactPhone") String contactPhone,
                          @Param("contactEmail") String contactEmail,
                          @Param("domain") String domain,
                          @Param("status") String status,
                          @Param("createTime") LocalDateTime createTime);

  /**
   * 更新租户信息
   *
   * @param tenantId 租户ID
   * @param tenant   租户信息
   * @return 更新行数
   */
  int updateTenantInfo(@Param("tenantId") Long tenantId, @Param("tenant") UpdateTenantDTO tenant);
}



package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysApi;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiMapper {
  SysApi selectById(@Param("id") Long id);

  List<SysApi> selectAll();

  int insert(SysApi api);

  int update(SysApi api);

  int deleteById(@Param("id") Long id);

  List<SysApi> selectByRoleId(@Param("roleId") Long roleId);

  int deleteBindingsByRoleId(@Param("roleId") Long roleId);

  int bindRoleApis(@Param("roleId") Long roleId, @Param("apiIds") List<Long> apiIds);

  /**
   * 检查角色和API的关联是否存在
   *
   * @param roleId 角色ID
   * @param apiId  API ID
   * @return 存在返回1，不存在返回0
   */
  int existsRoleApiBinding(@Param("roleId") Long roleId, @Param("apiId") Long apiId);

  /**
   * 删除角色和API的单个关联
   *
   * @param roleId 角色ID
   * @param apiId  API ID
   * @return 删除行数
   */
  int deleteRoleApiBinding(@Param("roleId") Long roleId, @Param("apiId") Long apiId);

  /**
   * 插入角色和API的单个关联
   *
   * @param roleId 角色ID
   * @param apiId  API ID
   * @return 插入行数
   */
  int insertRoleApiBinding(@Param("roleId") Long roleId, @Param("apiId") Long apiId);

  int countByRoleNamesAndApi(@Param("roleNames") List<String> roleNames,
                             @Param("method") String method,
                             @Param("path") String path,
                             @Param("permlabel") String permlabel);

  List<SysApi> selectByRoleNames(@Param("roleNames") List<String> roleNames);

  /**
   * 分页查询API列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return API列表
   */
  List<SysApi> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询API列表（带条件）
   *
   * @param method      HTTP方法（模糊搜索）
   * @param path        API路径（模糊搜索）
   * @param permlabel   权限标签（模糊搜索）
   * @param moduleKey   模块键（模糊搜索）
   * @param createTime  创建时间（查询此时间之前的数据）
   * @param offset      偏移量
   * @param limit       限制数量
   * @return API列表
   */
  List<SysApi> selectPageWithCondition(@Param("method") String method,
                                         @Param("path") String path,
                                         @Param("permlabel") String permlabel,
                                         @Param("moduleKey") String moduleKey,
                                         @Param("appCode") String appCode,
                                         @Param("createTime") java.time.LocalDateTime createTime,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

  /**
   * 统计API总数
   *
   * @return API总数
   */
  Long countAll();

  /**
   * 统计API总数（带条件）
   *
   * @param method      HTTP方法（模糊搜索）
   * @param path        API路径（模糊搜索）
   * @param permlabel   权限标签（模糊搜索）
   * @param moduleKey   模块键（模糊搜索）
   * @param createTime  创建时间（查询此时间之前的数据）
   * @return API总数
   */
  Long countWithCondition(@Param("method") String method,
                           @Param("path") String path,
                           @Param("permlabel") String permlabel,
                           @Param("moduleKey") String moduleKey,
                           @Param("appCode") String appCode,
                           @Param("createTime") java.time.LocalDateTime createTime);

  /**
   * 更新API信息
   *
   * @param apiId API ID
   * @param api   API信息
   * @return 更新行数
   */
  int updateApiInfo(@Param("apiId") Long apiId, @Param("api") cn.skylark.permission.authorization.dto.UpdateApiDTO api);

  /**
   * 按权限标识查询API ID
   */
  List<Long> selectIdsByPermlabels(@Param("permlabels") List<String> permlabels);

  /**
   * 回收：删除角色绑定中“不在允许集合”里的API。
   * @param roleId 角色ID
   * @param allowedApiIds 允许的API ID集合（为空表示全部删除）
   * @return 删除行数
   */
  int deleteRoleApisNotInAllowed(@Param("roleId") Long roleId, @Param("allowedApiIds") List<Long> allowedApiIds);
}

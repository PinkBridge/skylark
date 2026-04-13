package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志Mapper
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Mapper
public interface LoginLogMapper {

  /**
   * 根据ID查询登录日志
   *
   * @param id 日志ID
   * @return 登录日志
   */
  SysLoginLog selectById(@Param("id") Long id);

  /**
   * 查询所有登录日志
   *
   * @return 登录日志列表
   */
  List<SysLoginLog> selectAll();

  /**
   * 插入登录日志
   *
   * @param loginLog 登录日志
   * @return 插入行数
   */
  int insert(SysLoginLog loginLog);

  /**
   * 更新登录日志
   *
   * @param loginLog 登录日志
   * @return 更新行数
   */
  int update(SysLoginLog loginLog);

  /**
   * 根据ID删除登录日志
   *
   * @param id 日志ID
   * @return 删除行数
   */
  int deleteById(@Param("id") Long id);

  /**
   * 分页查询登录日志列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 登录日志列表
   */
  List<SysLoginLog> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询登录日志列表（带条件）
   *
   * @param username       用户名（模糊搜索）
   * @param loginIp        登录IP（模糊搜索）
   * @param loginStatus    登录状态
   * @param loginTimeStart 登录时间开始
   * @param loginTimeEnd   登录时间结束
   * @param tenantId       租户ID
   * @param clientId       应用ID（模糊搜索）
   * @param offset         偏移量
   * @param limit          限制数量
   * @return 登录日志列表
   */
  List<SysLoginLog> selectPageWithCondition(@Param("username") String username,
                                             @Param("loginIp") String loginIp,
                                             @Param("loginStatus") String loginStatus,
                                             @Param("loginTimeStart") java.time.LocalDateTime loginTimeStart,
                                             @Param("loginTimeEnd") java.time.LocalDateTime loginTimeEnd,
                                             @Param("tenantId") Long tenantId,
                                             @Param("clientId") String clientId,
                                             @Param("offset") Integer offset,
                                             @Param("limit") Integer limit);

  /**
   * 统计登录日志总数
   *
   * @return 登录日志总数
   */
  Long countAll();

  /**
   * 统计登录日志总数（带条件）
   *
   * @param username       用户名（模糊搜索）
   * @param loginIp        登录IP（模糊搜索）
   * @param loginStatus    登录状态
   * @param loginTimeStart 登录时间开始
   * @param loginTimeEnd   登录时间结束
   * @param tenantId       租户ID
   * @param clientId       应用ID（模糊搜索）
   * @return 登录日志总数
   */
  Long countWithCondition(@Param("username") String username,
                           @Param("loginIp") String loginIp,
                           @Param("loginStatus") String loginStatus,
                           @Param("loginTimeStart") java.time.LocalDateTime loginTimeStart,
                           @Param("loginTimeEnd") java.time.LocalDateTime loginTimeEnd,
                           @Param("tenantId") Long tenantId,
                           @Param("clientId") String clientId);
}


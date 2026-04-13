package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.dto.UpdateUserDTO;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface UserMapper {

  /**
   * find user by username
   *
   * @param username username
   * @return user information
   */
  SysUser findByUsername(@Param("username") String username);

  SysUser selectById(@Param("id") Long id);

  List<SysUser> selectAll();

  int insert(SysUser user);

  int update(SysUser user);

  int deleteById(@Param("id") Long id);

  List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

  void deleteUserRoles(@Param("userId") Long userId);

  void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

  /**
   * 更新用户密码
   *
   * @param userId   用户ID
   * @param password 新密码
   * @return 更新行数
   */
  int updatePassword(@Param("userId") Long userId, @Param("password") String password);

  /**
   * 将用户归属到指定租户（WHERE 中含 tenant_id，避免 TenantInterceptor 再追加当前上下文租户条件）。
   * 用于「为某租户新建管理员」等必须写入目标租户而非请求头租户的场景。
   */
  int updateTenantIdByUserId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

  /**
   * 分页查询用户列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 用户列表
   */
  List<SysUser> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询用户列表（带条件）
   *
   * @param username   用户名（模糊搜索）
   * @param phone      手机号（模糊搜索）
   * @param email      邮箱（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据）
   * @param offset     偏移量
   * @param limit      限制数量
   * @return 用户列表
   */
  List<SysUser> selectPageWithCondition(@Param("username") String username,
                                         @Param("phone") String phone,
                                         @Param("email") String email,
                                         @Param("createTime") java.time.LocalDateTime createTime,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

  /**
   * 统计用户总数
   *
   * @return 用户总数
   */
  Long countAll();

  /**
   * 统计用户总数（带条件）
   *
   * @param username   用户名（模糊搜索）
   * @param phone      手机号（模糊搜索）
   * @param email      邮箱（模糊搜索）
   * @param createTime 创建时间（查询此时间之前的数据）
   * @return 用户总数
   */
  Long countWithCondition(@Param("username") String username,
                           @Param("phone") String phone,
                           @Param("email") String email,
                           @Param("createTime") java.time.LocalDateTime createTime);

  /**
   * 更新用户信息（不包含密码）
   *
   * @param userId 用户ID
   * @param user   用户信息
   * @return 更新行数
   */
  int updateUserInfo(@Param("userId") Long userId, @Param("user") UpdateUserDTO user);
}

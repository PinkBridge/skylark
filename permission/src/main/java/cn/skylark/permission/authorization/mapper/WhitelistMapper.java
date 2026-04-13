package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysWhitelist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 白名单 Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface WhitelistMapper {

  /**
   * 根据ID查询
   */
  SysWhitelist selectById(@Param("id") Long id);

  /**
   * 查询所有
   */
  List<SysWhitelist> selectAll();

  /**
   * 查询所有启用的白名单
   */
  List<SysWhitelist> selectEnabled();

  /**
   * 新增
   */
  int insert(SysWhitelist whitelist);

  /**
   * 更新
   */
  int update(SysWhitelist whitelist);

  /**
   * 删除
   */
  int deleteById(@Param("id") Long id);

  /**
   * 分页查询白名单列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 白名单列表
   */
  List<SysWhitelist> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询白名单列表（带条件）
   *
   * @param method      HTTP方法（模糊搜索）
   * @param path        API路径（模糊搜索）
   * @param remark      备注（模糊搜索）
   * @param enabled     是否启用（精确搜索）
   * @param createTime  创建时间（查询此时间之前的数据）
   * @param offset      偏移量
   * @param limit       限制数量
   * @return 白名单列表
   */
  List<SysWhitelist> selectPageWithCondition(@Param("method") String method,
                                              @Param("path") String path,
                                              @Param("remark") String remark,
                                              @Param("enabled") Boolean enabled,
                                              @Param("createTime") java.time.LocalDateTime createTime,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

  /**
   * 统计白名单总数
   *
   * @return 白名单总数
   */
  Long countAll();

  /**
   * 统计白名单总数（带条件）
   *
   * @param method      HTTP方法（模糊搜索）
   * @param path        API路径（模糊搜索）
   * @param remark      备注（模糊搜索）
   * @param enabled     是否启用（精确搜索）
   * @param createTime  创建时间（查询此时间之前的数据）
   * @return 白名单总数
   */
  Long countWithCondition(@Param("method") String method,
                           @Param("path") String path,
                           @Param("remark") String remark,
                           @Param("enabled") Boolean enabled,
                           @Param("createTime") java.time.LocalDateTime createTime);

  /**
   * 更新白名单信息
   *
   * @param whitelistId 白名单ID
   * @param whitelist   白名单信息
   * @return 更新行数
   */
  int updateWhitelistInfo(@Param("whitelistId") Long whitelistId, @Param("whitelist") cn.skylark.permission.authorization.dto.UpdateWhitelistDTO whitelist);
}


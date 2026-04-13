package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.dto.UpdateOauthClientDTO;
import cn.skylark.permission.authorization.entity.OauthClientDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OAuth2客户端 Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface OauthClientMapper {

  /**
   * 根据客户端ID查询
   */
  OauthClientDetails selectByClientId(@Param("clientId") String clientId);

  /**
   * 查询所有
   */
  List<OauthClientDetails> selectAll();

  /**
   * 新增
   */
  int insert(OauthClientDetails client);

  /**
   * 更新
   */
  int update(OauthClientDetails client);

  /**
   * 删除
   */
  int deleteByClientId(@Param("clientId") String clientId);

  /**
   * 分页查询客户端列表
   *
   * @param offset 偏移量
   * @param limit  限制数量
   * @return 客户端列表
   */
  List<OauthClientDetails> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  /**
   * 分页查询客户端列表（带条件）
   *
   * @param clientId            客户端ID（模糊搜索）
   * @param authorizedGrantTypes 授权模式（模糊搜索）
   * @param scope               作用域（模糊搜索）
   * @param offset              偏移量
   * @param limit               限制数量
   * @return 客户端列表
   */
  List<OauthClientDetails> selectPageWithCondition(@Param("clientId") String clientId,
                                                    @Param("authorizedGrantTypes") String authorizedGrantTypes,
                                                    @Param("scope") String scope,
                                                    @Param("offset") Integer offset,
                                                    @Param("limit") Integer limit);

  /**
   * 统计客户端总数
   *
   * @return 客户端总数
   */
  Long countAll();

  /**
   * 统计客户端总数（带条件）
   *
   * @param clientId            客户端ID（模糊搜索）
   * @param authorizedGrantTypes 授权模式（模糊搜索）
   * @param scope               作用域（模糊搜索）
   * @return 客户端总数
   */
  Long countWithCondition(@Param("clientId") String clientId,
                           @Param("authorizedGrantTypes") String authorizedGrantTypes,
                           @Param("scope") String scope);

  /**
   * 更新客户端信息
   *
   * @param clientId 客户端ID
   * @param client   客户端信息
   * @return 更新行数
   */
  int updateClientInfo(@Param("clientId") String clientId, @Param("client") UpdateOauthClientDTO client);

  /**
   * 仅更新客户端重定向地址
   *
   * @param clientId             客户端ID
   * @param webServerRedirectUri 重定向URI（逗号分隔）
   * @return 更新行数
   */
  int updateRedirectUriByClientId(@Param("clientId") String clientId,
                                  @Param("webServerRedirectUri") String webServerRedirectUri);
}

